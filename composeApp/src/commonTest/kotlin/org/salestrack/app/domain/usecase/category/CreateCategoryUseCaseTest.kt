package org.salestrack.app.domain.usecase.category

import kotlinx.coroutines.test.runTest
import org.salestrack.app.core.FakeTimeProvider
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.data.mock.MockCategoryFactory
import org.salestrack.app.data.repository.FakeCategoryRepository
import org.salestrack.app.data.source.InMemoryCategoryDataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateCategoryUseCaseTest {

    @Test
    fun should_create_category_when_input_is_valid() = runTest {
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(FakeTimeProvider(1_000L))),
            timeProvider = FakeTimeProvider(2_000L),
        )
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(name = "Limpieza", colorHex = "#AABBCC")

        assertTrue(result is AppResult.Success)
        assertEquals("Limpieza", result.value.name)
    }

    @Test
    fun should_fail_when_name_is_blank() = runTest {
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(FakeTimeProvider(1_000L))),
            timeProvider = FakeTimeProvider(2_000L),
        )
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(name = "", colorHex = "#AABBCC")

        assertTrue(result is AppResult.Failure)
    }

    @Test
    fun should_fail_when_color_is_invalid() = runTest {
        val repository = FakeCategoryRepository(
            dataSource = InMemoryCategoryDataSource(MockCategoryFactory.create(FakeTimeProvider(1_000L))),
            timeProvider = FakeTimeProvider(2_000L),
        )
        val useCase = CreateCategoryUseCase(repository)

        val result = useCase(name = "Limpieza", colorHex = "AZUL")

        assertTrue(result is AppResult.Failure)
    }
}
