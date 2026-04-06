package org.salestrack.app.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.domain.model.CatalogExportFile
import org.salestrack.app.domain.model.CatalogImportError
import org.salestrack.app.domain.model.CatalogImportResult
import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.StockAdjustmentType
import org.salestrack.app.domain.model.StockMovement

/**
 * Firebase-ready stub that keeps inventory contracts stable while backend is mocked.
 */
class FirestoreInventoryDataSource(
    initialProducts: List<Product>,
    initialMovements: List<StockMovement>,
    private val timeProvider: TimeProvider,
) : InventoryDataSource {

    private val productsState = MutableStateFlow(initialProducts)
    private val movementsState = MutableStateFlow(initialMovements)

    override fun observeProducts(): Flow<List<Product>> = productsState.asStateFlow()

    override fun observeStockMovements(productId: String?): Flow<List<StockMovement>> {
        return movementsState.asStateFlow().map { movements ->
            movements
                .asSequence()
                .filter { productId == null || it.productId == productId }
                .sortedByDescending { it.createdAtMillis }
                .toList()
        }
    }

    override suspend fun addProduct(input: NewProductInput): AppResult<Product> {
        val barcodeExists = input.barcode != null && productsState.value.any { it.barcode == input.barcode }
        if (barcodeExists) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        val now = timeProvider.nowMillis()
        val product = Product(
            id = "FI-$now-${productsState.value.size + 1}",
            name = input.name.trim(),
            description = input.description.trim(),
            unitPrice = input.unitPrice,
            unit = input.unit.trim(),
            barcode = input.barcode?.trim()?.ifBlank { null },
            category = input.category.trim(),
            stock = input.initialStock,
            minimumStock = input.minimumStock,
        )
        productsState.value = productsState.value + product
        appendMovement(
            productId = product.id,
            type = StockAdjustmentType.Entry,
            quantityDelta = input.initialStock,
            reason = "Stock inicial",
            platform = "Stub",
        )

        return AppResult.Success(product)
    }

    override suspend fun updateProduct(product: Product): AppResult<Product> {
        val current = productsState.value
        val index = current.indexOfFirst { it.id == product.id }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        val duplicatedBarcode = product.barcode != null &&
            current.any { it.id != product.id && it.barcode == product.barcode }
        if (duplicatedBarcode) {
            return AppResult.Failure(IllegalStateException("El codigo de barras ya existe"))
        }

        productsState.value = current.toMutableList().apply { set(index, product) }
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
        val current = productsState.value
        val index = current.indexOfFirst { it.id == productId }
        if (index < 0) {
            return AppResult.Failure(NoSuchElementException("Producto no encontrado"))
        }

        val baseProduct = current[index]
        val newStock = (baseProduct.stock + quantityDelta).coerceAtLeast(0)
        val appliedDelta = newStock - baseProduct.stock
        val updated = baseProduct.copy(stock = newStock)
        productsState.value = current.toMutableList().apply { set(index, updated) }

        appendMovement(
            productId = productId,
            type = type,
            quantityDelta = appliedDelta,
            reason = reason,
            sellerName = sellerName,
            platform = platform,
        )

        return AppResult.Success(updated)
    }

    override suspend fun deductStock(
        productId: String,
        quantity: Int,
        reason: String,
        sellerName: String?,
        platform: String?,
    ): AppResult<Product> {
        val product = productsState.value.firstOrNull { it.id == productId }
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
        val rows = productsState.value.map { product ->
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
        val rows = productsState.value.joinToString(separator = "") { product ->
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

    override suspend fun getLowStockProducts(): AppResult<List<Product>> {
        val lowStock = productsState.value
            .filter { it.isActive }
            .filter { it.stock <= it.minimumStock }
            .sortedBy { it.stock }
        return AppResult.Success(lowStock)
    }

    private fun appendMovement(
        productId: String,
        type: StockAdjustmentType,
        quantityDelta: Int,
        reason: String,
        sellerName: String? = null,
        platform: String? = null,
    ) {
        val now = timeProvider.nowMillis()
        movementsState.value = movementsState.value + StockMovement(
            id = "FM-$now-$productId-${movementsState.value.size + 1}",
            productId = productId,
            type = type,
            quantityDelta = quantityDelta,
            reason = reason,
            sellerName = sellerName,
            platform = platform,
            createdAtMillis = now,
        )
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
