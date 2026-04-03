package org.salestrack.app.presentation.feature.category

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeDispatcherProvider
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.data.mock.MockCategoryFactory
import org.salestrack.app.data.repository.FakeCategoryRepository
import org.salestrack.app.data.source.InMemoryCategoryDataSource
import org.salestrack.app.domain.usecase.category.CreateCategoryUseCase
import org.salestrack.app.domain.usecase.category.DeleteCategoryUseCase
import org.salestrack.app.domain.usecase.category.ObserveCategoriesUseCase
import org.salestrack.app.domain.usecase.category.UpdateCategoryUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryManagementViewModelTest {

    @Test
    fun should_load_categories_on_start() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )

        val viewModel = CategoryManagementViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            createCategoryUseCase = CreateCategoryUseCase(repository),
            updateCategoryUseCase = UpdateCategoryUseCase(repository),
            deleteCategoryUseCase = DeleteCategoryUseCase(repository),
        )

        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.categories.size)
        assertTrue(!viewModel.state.value.isLoading)
    }

    @Test
    fun should_create_category_when_save_new_is_triggered() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )

        val viewModel = CategoryManagementViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            createCategoryUseCase = CreateCategoryUseCase(repository),
            updateCategoryUseCase = UpdateCategoryUseCase(repository),
            deleteCategoryUseCase = DeleteCategoryUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onEvent(CategoryManagementUiEvent.NewNameChanged("Tecnologia"))
        viewModel.onEvent(CategoryManagementUiEvent.NewColorChanged("#9C27B0"))
        viewModel.onEvent(CategoryManagementUiEvent.SaveNewCategory)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.any { it.name == "Tecnologia" })
    }

    @Test
    fun should_edit_existing_category() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )

        val viewModel = CategoryManagementViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            createCategoryUseCase = CreateCategoryUseCase(repository),
            updateCategoryUseCase = UpdateCategoryUseCase(repository),
            deleteCategoryUseCase = DeleteCategoryUseCase(repository),
        )

        advanceUntilIdle()
        val first = viewModel.state.value.categories.first()
        viewModel.onEvent(CategoryManagementUiEvent.StartEdit(first))
        viewModel.onEvent(CategoryManagementUiEvent.EditNameChanged("Bebidas Frias"))
        viewModel.onEvent(CategoryManagementUiEvent.EditColorChanged("#123456"))
        viewModel.onEvent(CategoryManagementUiEvent.SaveEditedCategory)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.categories.any { it.name == "Bebidas Frias" })
    }

    @Test
    fun should_show_error_when_new_category_is_invalid() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val timeProvider = FakeTimeProvider(1_000L)
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(timeProvider)),
            timeProvider = timeProvider,
        )

        val viewModel = CategoryManagementViewModel(
            dispatcherProvider = FakeDispatcherProvider(dispatcher),
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            createCategoryUseCase = CreateCategoryUseCase(repository),
            updateCategoryUseCase = UpdateCategoryUseCase(repository),
            deleteCategoryUseCase = DeleteCategoryUseCase(repository),
        )

        advanceUntilIdle()
        viewModel.onEvent(CategoryManagementUiEvent.NewNameChanged(""))
        viewModel.onEvent(CategoryManagementUiEvent.NewColorChanged("#1E88E5"))
        viewModel.onEvent(CategoryManagementUiEvent.SaveNewCategory)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.errorMessage != null)
    }
}
