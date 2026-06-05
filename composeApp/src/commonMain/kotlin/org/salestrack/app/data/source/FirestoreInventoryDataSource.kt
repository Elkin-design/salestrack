package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.firebase.FirebaseHelpers
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.model.CatalogImportError
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement
import dev.gitlive.firebase.firestore.Direction

class FirestoreInventoryDataSource(
    private val timeProvider: TimeProvider,
) : InventoryDataSource {

    private fun productsRef() = FirebaseHelpers.userRootDocument().collection("inventory")
    private fun movementsRef() = FirebaseHelpers.userRootDocument().collection("stock_movements")

    override fun observeProducts(): Flow<List<Product>> {
        return productsRef().snapshots.map { snapshot ->
            snapshot.documents.map { it.data<Product>() }.filter { it.isActive }
        }.catch { emit(emptyList()) }
    }

    override fun observeStockMovements(productId: String?): Flow<List<StockMovement>> {
        return movementsRef().orderBy("createdAtMillis", Direction.DESCENDING).snapshots.map { snap ->
            snap.documents.map { it.data<StockMovement>() }.filter { productId == null || it.productId == productId }
        }.catch { emit(emptyList()) }
    }

    override suspend fun addProduct(input: NewProductInput): AppResult<Product> {
        return try {
            val snapshot = productsRef().get()
            val products = snapshot.documents.map { it.data<Product>() }
            
            val barcodeExists = input.barcode != null && products.any { it.barcode == input.barcode }
            if (barcodeExists) {
                return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
            }

            val now = timeProvider.nowMillis()
            val id = "FI-$now-${products.size + 1}"
            val product = Product(
                id = id,
                name = input.name.trim(),
                description = input.description.trim(),
                unitPrice = input.unitPrice,
                unit = input.unit.trim(),
                barcode = input.barcode?.trim()?.ifBlank { null },
                category = input.category.trim(),
                stock = input.initialStock,
                minimumStock = input.minimumStock,
            )
            productsRef().document(id).set(product)
            
            appendMovement(
                productId = product.id,
                type = StockAdjustmentType.Entry,
                quantityDelta = input.initialStock,
                reason = "Stock inicial",
                platform = "System",
            )

            AppResult.Success(product)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun updateProduct(product: Product): AppResult<Product> {
        return try {
            val snapshot = productsRef().get()
            val products = snapshot.documents.map { it.data<Product>() }
            
            val duplicatedBarcode = product.barcode != null &&
                products.any { it.id != product.id && it.barcode == product.barcode }
            if (duplicatedBarcode) {
                return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
            }

            productsRef().document(product.id).set(product)
            AppResult.Success(product)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun adjustStock(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        return try {
            val doc = productsRef().document(productId).get()
            if (!doc.exists) {
                return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
            }
            val baseProduct = doc.data<Product>()
            val newStock = (baseProduct.stock + quantityDelta).coerceAtLeast(0)
            val appliedDelta = newStock - baseProduct.stock
            val updated = baseProduct.copy(stock = newStock)
            
            productsRef().document(productId).set(updated)

            appendMovement(
                productId = productId,
                type = type,
                quantityDelta = appliedDelta,
                reason = reason,
                sellerName = sellerName,
                platform = platform,
            )

            AppResult.Success(updated)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        return try {
            val doc = productsRef().document(productId).get()
            if (!doc.exists) return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
            
            val product = doc.data<Product>()
            if (product.stock < quantity) {
                return AppResult.Failure(IllegalStateException("Stock insuficiente"))
            }

            adjustStock(
                productId = productId,
                quantityDelta = -quantity,
                reason = reason,
                type = StockAdjustmentType.Sale,
                sellerName = sellerName,
                platform = platform,
            )
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun importCatalogCsv(csvContent: String): AppResult<CatalogImportResult> {
        return try {
            val rows = csvContent.lineSequence().map { it.trim() }.filter { it.isNotBlank() }.toList()
            if (rows.isEmpty()) return AppResult.Failure(IllegalArgumentException("CSV vacio"))

            val dataRows = if (rows.first().contains("name", ignoreCase = true)) rows.drop(1) else rows
            val errors = mutableListOf<CatalogImportError>()
            var imported = 0

            dataRows.forEachIndexed { index, row ->
                val lineNumber = index + 1
                val cols = row.split(",")
                if (cols.size < 8) {
                    errors += CatalogImportError(line = lineNumber, reason = "Columnas insuficientes")
                    return@forEachIndexed
                }

                val name = cols[0].trim()
                val description = cols[1].trim()
                val unitPrice = cols[2].trim().toDoubleOrNull()
                val unit = cols[3].trim()
                val barcode = cols[4].trim().ifBlank { null }
                val category = cols[5].trim()
                val stock = cols[6].trim().toIntOrNull()
                val minimum = cols[7].trim().toIntOrNull()

                if (name.isBlank() || unitPrice == null || unit.isBlank() || category.isBlank() || stock == null || minimum == null) {
                    errors += CatalogImportError(line = lineNumber, reason = "Campos invalidos")
                    return@forEachIndexed
                }

                val result = addProduct(
                    NewProductInput(
                        name = name,
                        description = description,
                        unitPrice = unitPrice,
                        unit = unit,
                        barcode = barcode,
                        category = category,
                        initialStock = stock,
                        minimumStock = minimum,
                    ),
                )
                when (result) {
                    is AppResult.Success -> imported++
                    is AppResult.Failure -> errors += CatalogImportError(
                        line = lineNumber,
                        reason = result.error.message ?: "Error importando fila",
                    )
                }
            }

            AppResult.Success(
                CatalogImportResult(totalRows = dataRows.size, importedRows = imported, failedRows = errors.size, errors = errors),
            )
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun exportCatalogCsv(): AppResult<CatalogExportFile> {
        return try {
            val prods = productsRef().get().documents.map { it.data<Product>() }
            val header = "name,description,unitPrice,unit,barcode,category,stock,minimumStock"
            val rows = prods.map { product ->
                listOf(
                    escapeCsv(product.name),
                    escapeCsv(product.description),
                    product.unitPrice.toString(),
                    escapeCsv(product.unit),
                    escapeCsv(product.barcode.orEmpty()),
                    escapeCsv(product.category),
                    product.stock.toString(),
                    product.minimumStock.toString(),
                ).joinToString(",")
            }
            AppResult.Success(CatalogExportFile("catalogo_inventario.csv", "text/csv", (listOf(header) + rows).joinToString("\n")))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun exportCatalogExcel(): AppResult<CatalogExportFile> {
        return try {
            val prods = productsRef().get().documents.map { it.data<Product>() }
            val header = "<?xml version=\"1.0\"?><Workbook><Worksheet name=\"Catalogo\"><Table>"
            val columns = "<Row><Cell><Data>Name</Data></Cell><Cell><Data>Description</Data></Cell><Cell><Data>UnitPrice</Data></Cell><Cell><Data>Unit</Data></Cell><Cell><Data>Barcode</Data></Cell><Cell><Data>Category</Data></Cell><Cell><Data>Stock</Data></Cell><Cell><Data>MinimumStock</Data></Cell></Row>"
            val rows = prods.joinToString(separator = "") { product ->
                "<Row>" +
                    "<Cell><Data>${escapeXml(product.name)}</Data></Cell>" +
                    "<Cell><Data>${escapeXml(product.description)}</Data></Cell>" +
                    "<Cell><Data>${product.unitPrice}</Data></Cell>" +
                    "<Cell><Data>${escapeXml(product.unit)}</Data></Cell>" +
                    "<Cell><Data>${escapeXml(product.barcode.orEmpty())}</Data></Cell>" +
                    "<Cell><Data>${escapeXml(product.category)}</Data></Cell>" +
                    "<Cell><Data>${product.stock}</Data></Cell>" +
                    "<Cell><Data>${product.minimumStock}</Data></Cell>" +
                    "</Row>"
            }
            val footer = "</Table></Worksheet></Workbook>"

            AppResult.Success(CatalogExportFile("catalogo_inventario.xls", "application/vnd.ms-excel", header + columns + rows + footer))
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): AppResult<Unit> {
        return try {
            val doc = productsRef().document(productId).get()
            if (!doc.exists) {
                return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
            }
            val product = doc.data<Product>()
            val updated = product.copy(isActive = false)
            productsRef().document(productId).set(updated)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    override suspend fun getLowStockProducts(): AppResult<List<Product>> {
        return try {
            val prods = productsRef().get().documents.map { it.data<Product>() }
            val lowStock = prods.filter { it.isActive && it.stock <= it.minimumStock }.sortedBy { it.stock }
            AppResult.Success(lowStock)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }

    private suspend fun appendMovement(
        productId: String,
        type: StockAdjustmentType,
        quantityDelta: Int,
        reason: String,
        sellerName: String? = null,
        platform: String? = null,
    ) {
        val now = timeProvider.nowMillis()
        val id = "FM-$now-$productId"
        val movement = StockMovement(
            id = id,
            productId = productId,
            type = type,
            quantityDelta = quantityDelta,
            reason = reason,
            sellerName = sellerName,
            platform = platform,
            createdAtMillis = now,
        )
        movementsRef().document(id).set(movement)
    }

    private fun escapeCsv(value: String): String {
        val mustQuote = value.contains(',') || value.contains('"') || value.contains('\n')
        if (!mustQuote) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun escapeXml(value: String): String {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
    }
}
