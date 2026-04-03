package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.Category

class InMemoryCategoryDataSource(initialCategories: List<Category>) {
    private val categoriesState = MutableStateFlow(initialCategories)

    fun observe(): StateFlow<List<Category>> = categoriesState.asStateFlow()

    fun getCurrent(): List<Category> = categoriesState.value

    fun replaceAll(categories: List<Category>) {
        categoriesState.value = categories
    }
}
