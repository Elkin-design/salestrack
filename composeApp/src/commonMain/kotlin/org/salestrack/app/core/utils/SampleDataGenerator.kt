package org.salestrack.app.core.utils

import org.salestrack.app.domain.model.NewProductInput
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Product
import kotlin.random.Random

/**
 * Utilidad para generar datos de prueba realistas para un restaurante.
 */
object SampleDataGenerator {

    private val sellers = listOf("Carlos mesero", "Juan mesa 1", "Maria caja", "Admin")
    private val unit = "Unidad"

    fun getRestaurantProducts(): List<NewProductInput> = listOf(
        NewProductInput("Hamburguesa Especial", "Doble carne, queso cheddar, tocino y papas", 18.5, unit, "HB-001", "Hamburguesas", 100, 10),
        NewProductInput("Hamburguesa Clásica", "Carne 150g, lechuga, tomate y cebolla", 14.0, unit, "HB-002", "Hamburguesas", 120, 15),
        NewProductInput("Pizza Margherita", "Albahaca fresca, mozzarella y salsa pomodoro", 22.0, unit, "PZ-001", "Pizzas", 50, 5),
        NewProductInput("Pizza Pepperoni", "Pepperoni premium y abundante queso mozzarella", 25.5, unit, "PZ-002", "Pizzas", 60, 5),
        NewProductInput("Alitas BBQ x12", "Alitas crocantes bañadas en salsa BBQ artesanal", 16.0, unit, "ET-001", "Entradas", 80, 10),
        NewProductInput("Papas Fritas Grandes", "Papas de la casa con sal de mar", 6.5, unit, "ET-002", "Entradas", 200, 20),
        NewProductInput("Coca-Cola 350ml", "Refresco embotellado frío", 3.5, unit, "BE-001", "Bebidas", 300, 50),
        NewProductInput("Limonada Cerezada", "Limonada fresca con jarabe de cerezas", 5.5, unit, "BE-002", "Bebidas", 100, 10),
        NewProductInput("Cerveza Artesanal", "Cerveza tipo APA elaborada localmente", 8.0, unit, "BE-003", "Bebidas", 150, 20),
        NewProductInput("Brownie con Helado", "Brownie tibio con helado de vainilla", 9.0, unit, "PS-001", "Postres", 40, 5)
    )

    fun generateSales(products: List<Product>, nowMillis: Long): List<NewSaleInput> {
        val sales = mutableListOf<NewSaleInput>()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneMonthMillis = 30 * oneDayMillis
        val oneYearMillis = 365 * oneDayMillis

        // 1. Ventas de Hoy (50 ventas)
        repeat(50) {
            val product = products.random()
            val offset = Random.nextLong(0, oneDayMillis)
            sales.add(createSaleInput(product, nowMillis - offset))
        }

        // 2. Ventas de esta semana (100 ventas, excluyendo hoy)
        repeat(100) {
            val product = products.random()
            val offset = Random.nextLong(oneDayMillis, 7 * oneDayMillis)
            sales.add(createSaleInput(product, nowMillis - offset))
        }

        // 3. Ventas de este mes (150 ventas, excluyendo esta semana)
        repeat(150) {
            val product = products.random()
            val offset = Random.nextLong(7 * oneDayMillis, oneMonthMillis)
            sales.add(createSaleInput(product, nowMillis - offset))
        }

        // 4. Ventas del resto del año (300 ventas, distribuidas mensualmente)
        repeat(300) {
            val product = products.random()
            val offset = Random.nextLong(oneMonthMillis, oneYearMillis)
            sales.add(createSaleInput(product, nowMillis - offset))
        }

        return sales
    }

    private fun createSaleInput(product: Product, timestamp: Long): NewSaleInput {
        val qty = Random.nextInt(1, 4)
        // Ocasionalmente aplicamos un descuento pequeño (5-10%)
        val discount = if (Random.nextFloat() > 0.8) (product.unitPrice * qty * 0.1) else 0.0
        
        return NewSaleInput(
            productName = product.name,
            category = product.category,
            quantity = qty,
            unitPrice = product.unitPrice,
            discount = discount,
            sellerName = sellers.random(),
            productId = product.id,
            createdAtMillis = timestamp
        )
    }
}
