package org.salestrack.app.data.source

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.salestrack.app.domain.model.Sale

class InMemorySaleDataSource(initialSales: List<Sale>) {
    private val salesState = MutableStateFlow(initialSales)

    fun observe(): StateFlow<List<Sale>> = salesState.asStateFlow()

    fun getCurrent(): List<Sale> = salesState.value

    fun replaceAll(items: List<Sale>) {
        salesState.value = items
    }
}

