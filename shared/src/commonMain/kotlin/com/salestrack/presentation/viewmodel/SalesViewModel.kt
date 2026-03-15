package com.salestrack.presentation.viewmodel

import com.salestrack.domain.model.Sale
import com.salestrack.domain.repository.SalesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Base ViewModel class for KMP (Simplified)
abstract class BaseViewModel {
    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
}

class SalesViewModel(private val repository: SalesRepository) : BaseViewModel() {
    private val _salesState = MutableStateFlow<List<Sale>>(emptyList())
    val salesState: StateFlow<List<Sale>> = _salesState.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            repository.getSales().collect {
                _salesState.value = it
            }
        }
    }

    fun addSale(sale: Sale) {
        viewModelScope.launch {
            repository.addSale(sale)
        }
    }
}
