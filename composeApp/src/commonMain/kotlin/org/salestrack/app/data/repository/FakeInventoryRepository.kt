package org.salestrack.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.source.InMemoryInventoryDataSource
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.model.CatalogImportError
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement
import org.salestrack.app.domain.repository.InventoryRepository

class FakeInventoryRepository(
    private val dataSource: InMemoryInventoryDataSource,
    private val timeProvider: TimeProvider,
) : InventoryRepository {

    override fun observeProducts(): Flow<List<Product>> = dataSource.observeProducts()

    override fun observeStockMovements(productId: String?): Flow<List<StockMovement>> {
        return dataSource.observeMovements().map { movements ->
            movements
                .asSequence()
                .filter { productId == null || it.productId == productId }
                .sortedByDescending { it.createdAtMillis }
                .toList()
        }
    }

    override suspend fun addProduct(input: NewProductInput): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        if (input.barcode != null && currentProducts.any { it.barcode == input.barcode }) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        val product = Product(
            id = "P-${timeProvider.nowMillis()}-${currentProducts.size + 1}",
            name = input.name,
            description = input.description,
            unitPrice = input.unitPrice,
            unit = input.unit,
            barcode = input.barcode,
            category = input.category,
            stock = input.initialStock,
            minimumStock = input.minimumStock,
        )

        dataSource.replaceProducts(currentProducts + product)
        dataSource.appendMovement(
            StockMovement(
                id = "M-${timeProvider.nowMillis()}",
                productId = product.id,
                type = StockAdjustmentType.Entry,
                quantityDelta = input.initialStock,
                reason = "Stock inicial",
                platform = "Seed",
                createdAtMillis = timeProvider.nowMillis(),
            ),
        )

        return AppResult.Success(product)
    }

    override suspend fun updateProduct(product: Product): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        val index = currentProducts.indexOfFirst { it.id == product.id }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        if (product.barcode != null && currentProducts.any { it.id != product.id && it.barcode == product.barcode }) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        val updated = currentProducts.toMutableList().apply { set(index, product) }
        dataSource.replaceProducts(updated)
        return AppResult.Success(product)
    }

    override suspend fun adjustStock(
        productId: String,
        quantityDelta: Int,
        reason: String,
        type: StockAdjustmentType,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        val currentProducts = dataSource.getCurrentProducts()
        val index = currentProducts.indexOfFirst { it.id == productId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        val currentProduct = currentProducts[index]
        val newStock = (currentProduct.stock + quantityDelta).coerceAtLeast(0)
        val appliedDelta = newStock - currentProduct.stock
        val updatedProduct = currentProduct.copy(stock = newStock)

        val updatedProducts = currentProducts.toMutableList().apply { set(index, updatedProduct) }
        dataSource.replaceProducts(updatedProducts)

        dataSource.appendMovement(
            StockMovement(
                id = "M-${timeProvider.nowMillis()}-${productId}",
                productId = productId,
                type = type,
                quantityDelta = appliedDelta,
                reason = reason,
                sellerName = sellerName,
                platform = platform,
                createdAtMillis = timeProvider.nowMillis(),
            ),
        )

        return AppResult.Success(updatedProduct)
    }

    override suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        val product = dataSource.getCurrentProducts().firstOrNull { it.id == productId }
            ?: return AppResult.Failure(NoSuchElementException("Producto no encontrado"))

        if (product.stock < quantity) {
            return AppResult.Failure(IllegalStateException("Stock insuficiente"))
        }

        return adjustStock(
            productId = productId,
            quantityDelta = -quantity,
            reason = reason,
            type = StockAdjustmentType.Sale,
            sellerName = sellerName,
            platform = platform,
        )
    }

    override suspend fun importCatalogCsv(csvContent: String): AppResult<CatalogImportResult> {
        val rows = csvContent
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        if (rows.isEmpty()) {
            return AppResult.Failure(IllegalArgumentException("CSV vacio"))
        }

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

            val unitPrice = cols[2].trim().toDoubleOrNull()
            val stock = cols[6].trim().toIntOrNull()
            val minimumStock = cols[7].trim().toIntOrNull()
            if (unitPrice == null || stock == null || minimumStock == null) {
                errors += CatalogImportError(line = lineNumber, reason = "Formato numerico invalido")
                return@forEachIndexed
            }

            val result = addProduct(
                NewProductInput(
                    name = cols[0].trim(),
                    description = cols[1].trim(),
                    unitPrice = unitPrice,
                    unit = cols[3].trim(),
                    barcode = cols[4].trim().ifBlank { null },
                    category = cols[5].trim(),
                    initialStock = stock,
                    minimumStock = minimumStock,
                ),
            )

            when (result) {
                is AppResult.Success -> imported++
                is AppResult.Failure -> {
                    errors += CatalogImportError(
                        line = lineNumber,
                        reason = result.error.message ?: "Error importando fila",
                    )
                }
            }
        }

        return AppResult.Success(
            CatalogImportResult(
                totalRows = dataRows.size,
                importedRows = imported,
                failedRows = errors.size,
                errors = errors,
            ),
        )
    }

    override suspend fun exportCatalogCsv(): AppResult<CatalogExportFile> {
        val header = "name,description,unitPrice,unit,barcode,category,stock,minimumStock"
        val rows = dataSource.getCurrentProducts().map { product ->
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

        return AppResult.Success(
            CatalogExportFile(
                fileName = "catalogo_inventario.csv",
                mimeType = "text/csv",
                content = (listOf(header) + rows).joinToString("\n"),
            ),
        )
    }

    override suspend fun exportCatalogExcel(): AppResult<CatalogExportFile> {
        val header = "<?xml version=\"1.0\"?><Workbook><Worksheet name=\"Catalogo\"><Table>"
        val columns = "<Row><Cell><Data>Name</Data></Cell><Cell><Data>Description</Data></Cell><Cell><Data>UnitPrice</Data></Cell><Cell><Data>Unit</Data></Cell><Cell><Data>Barcode</Data></Cell><Cell><Data>Category</Data></Cell><Cell><Data>Stock</Data></Cell><Cell><Data>MinimumStock</Data></Cell></Row>"
        val rows = dataSource.getCurrentProducts().joinToString(separator = "") { product ->
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

        return AppResult.Success(
            CatalogExportFile(
                fileName = "catalogo_inventario.xls",
                mimeType = "application/vnd.ms-excel",
                content = header + columns + rows + footer,
            ),
        )
    }

    override suspend fun deleteProduct(productId: String): AppResult<Unit> {
        val currentProducts = dataSource.getCurrentProducts()
        val index = currentProducts.indexOfFirst { it.id == productId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }
        val updated = currentProducts.toMutableList()
        val product = updated[index]
        updated[index] = product.copy(isActive = false)
        dataSource.replaceProducts(updated)
        return AppResult.Success(Unit)
    }

    override suspend fun getLowStockProducts(): AppResult<List<Product>> {
        val lowStock = dataSource.getCurrentProducts()
            .filter { it.isActive }
            .filter { it.stock <= it.minimumStock }
            .sortedBy { it.stock }
        return AppResult.Success(lowStock)
    }

    private fun escapeCsv(value: String): String {
        val mustQuote = value.contains(',') || value.contains('"') || value.contains('\n')
        if (!mustQuote) return value
        return "\"${value.replace("\"", "\"\"")}\""
    }

    private fun escapeXml(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

