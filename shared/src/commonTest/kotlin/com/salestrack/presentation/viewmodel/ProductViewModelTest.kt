package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.Product
import com.salestrack.domain.repository.ProductRepository
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
class ProductViewModelTest {

    private val repository: ProductRepository = mockk()
    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { repository.getProducts() } returns flowOf(emptyList())
        viewModel = ProductViewModel(repository)
    }

    @Test
    fun `loadProducts updates productsState`() {
        val products = listOf(
            Product("1", "P1", "D1", 10.0, "Unit", null, "cat1", 10, 5)
        )
        coEvery { repository.getProducts() } returns flowOf(products)
        
        // This will trigger loadProducts in init or we can call it if needed
        // Since init calls loadProducts, we re-init for test
        viewModel = ProductViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(products, viewModel.productsState.value)
    }

    @Test
    fun `updateStock calls repository updateStock`() {
        val product = Product("1", "P1", "D1", 10.0, "Unit", null, "cat1", 10, 5)
        coEvery { repository.getProducts() } returns flowOf(listOf(product))
        coEvery { repository.updateStock(any(), any()) } returns Unit

        viewModel = ProductViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateStock("1", 5)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { repository.updateStock("1", 15) }
    }
}
