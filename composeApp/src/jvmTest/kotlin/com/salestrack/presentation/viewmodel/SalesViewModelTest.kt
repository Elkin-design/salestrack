package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.Product
import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.ProductRepository
import com.salestrack.domain.repository.SalesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SalesViewModelTest {

    private val repository: SalesRepository = mockk()
    private val productRepository: ProductRepository = mockk()
    private lateinit var viewModel: SalesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getSales() } returns flowOf(emptyList())
        viewModel = SalesViewModel(repository, productRepository)
    }

    @Test
    fun `loadSales updates salesState`() {
        val sales = listOf(
            Sale("1", "Prod1", "p1", 1, 10.0, 0.0, 10.0, "cat1", "v1", "Android", 123456789)
        )
        coEvery { repository.getSales() } returns flowOf(sales)

        viewModel = SalesViewModel(repository, productRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sales, viewModel.salesState.value)
    }

    @Test
    fun `addSale calls repository addSale`() {
        val sale = Sale("1", "Prod1", "p1", 1, 10.0, 0.0, 10.0, "cat1", "v1", "Android", 123456789)
        coEvery { repository.addSale(any()) } returns Unit

        viewModel.addSale(sale)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.addSale(sale) }
    }

    @Test
    fun `findProductByBarcode updates scannedProduct`() {
        val barcode = "12345"
        val product = Product("p1", "Prod1", "Desc", 10.0, "Unit", barcode, "cat1", 10, 5)
        coEvery { productRepository.getProductByBarcode(barcode) } returns product

        viewModel.findProductByBarcode(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(product, viewModel.scannedProduct.value)
    }
}
