package org.salestrack.app.domain.usecase.settings

import kotlinx.coroutines.flow.first
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.core.utils.SampleDataGenerator
import org.salestrack.app.core.utils.TimeProvider
import org.salestrack.app.data.source.InventoryDataSource
import org.salestrack.app.data.source.SaleDataSource
import org.salestrack.app.domain.model.Product

/**
 * Caso de uso para poblar la base de datos con datos de prueba de un restaurante.
 */
class PopulateSampleDataUseCase(
    private val inventoryDataSource: InventoryDataSource,
    private val saleDataSource: SaleDataSource,
    private val timeProvider: TimeProvider,
) {
    suspend fun execute(): AppResult<Unit> {
        return try {
            // 1. Opcional: Podríamos borrar data existente aquí, 
            // pero para ser seguros solo añadiremos por ahora o borraremos selectivamente.
            
            // 2. Generar y Guardar Productos
            val productInputs = SampleDataGenerator.getRestaurantProducts()
            val addedProducts = mutableListOf<Product>()
            
            for (input in productInputs) {
                val result = inventoryDataSource.addProduct(input)
                if (result is AppResult.Success) {
                    addedProducts.add(result.value)
                }
            }

            if (addedProducts.isEmpty()) {
                return AppResult.Failure(IllegalStateException("No se pudieron crear los productos"))
            }

            // 3. Generar y Guardar Ventas
            val now = timeProvider.nowMillis()
            val saleInputs = SampleDataGenerator.generateSales(addedProducts, now)
            
            for (saleInput in saleInputs) {
                saleDataSource.addSale(saleInput)
            }

            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(e)
        }
    }
}
