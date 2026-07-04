package org.salestrack.app.presentation.feature.sales

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider
import org.salestrack.app.core.presentation.BaseViewModel
import org.salestrack.app.core.result.AppResult
import org.salestrack.app.domain.model.NewSaleInput
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.SaleItem
import org.salestrack.app.domain.repository.InventoryRepository
import org.salestrack.app.domain.usecase.category.ObserveCategoriesUseCase
import org.salestrack.app.domain.usecase.sales.AddSaleUseCase

class PosViewModel(
    dispatcherProvider: DispatcherProvider,
    private val inventoryRepository: InventoryRepository,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase? = null,
    private val addSaleUseCase: AddSaleUseCase,
) : BaseViewModel<PosUiState, PosUiEvent, PosUiEffect>(
    initialState = PosUiState(),
    dispatcherProvider = dispatcherProvider,
) {
    init {
        observeInventory()
        observeCategories()
    }

    override fun onEvent(event: PosUiEvent) {
        when (event) {
            is PosUiEvent.QueryChanged -> {
                setState { it.copy(query = event.value) }
                applyFilters()
            }
            is PosUiEvent.CategoryChanged -> {
                setState { it.copy(selectedCategory = event.value) }
                applyFilters()
            }
            is PosUiEvent.AddToCart -> addToCart(event.product)
            is PosUiEvent.UpdateCartItemQuantity -> updateQuantity(event.productId, event.quantity)
            is PosUiEvent.RemoveFromCart -> removeFromCart(event.productId)
            PosUiEvent.ClearCart -> setState { it.copy(cart = emptyList(), globalDiscount = 0.0) }
            is PosUiEvent.ToggleCheckoutDialog -> setState { it.copy(isCheckoutDialogVisible = event.visible) }
            is PosUiEvent.PaymentMethodChanged -> setState { it.copy(selectedPaymentMethod = event.method) }
            is PosUiEvent.GlobalDiscountChanged -> setState { it.copy(globalDiscount = event.discount) }
            is PosUiEvent.ConfirmSale -> confirmSale(event.seller)
            PosUiEvent.Refresh -> applyFilters()
        }
    }

    private fun observeInventory() {
        scope.launch {
            inventoryRepository.observeProducts().collect { products ->
                setState { 
                    it.copy(inventoryProducts = products.filter { p -> p.isActive }) 
                }
                applyFilters()
            }
        }
    }

    private fun observeCategories() {
        val useCase = observeCategoriesUseCase ?: return
        scope.launch {
            useCase().collect { categories ->
                val activeCategories = categories.filter { it.isActive }
                val names = activeCategories.map { it.name }.distinct().sortedBy { it.lowercase() }
                setState { it.copy(isLoading = false, availableCategories = names) }
            }
        }
    }

    private fun applyFilters() {
        val current = state.value
        val filtered = current.inventoryProducts.filter { product ->
            val matchesQuery = if (current.query.isBlank()) true else product.name.contains(current.query, ignoreCase = true)
            val matchesCategory = if (current.selectedCategory == null) true else product.category == current.selectedCategory
            matchesQuery && matchesCategory
        }
        setState { it.copy(displayedProducts = filtered) }
    }

    private fun addToCart(product: Product) {
        val currentCart = state.value.cart.toMutableList()
        val existingIndex = currentCart.indexOfFirst { it.productId == product.id }
        
        if (existingIndex >= 0) {
            val existingItem = currentCart[existingIndex]
            currentCart[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(
                SaleItem(
                    productId = product.id,
                    productName = product.name,
                    category = product.category,
                    quantity = 1,
                    unitPrice = product.unitPrice,
                )
            )
        }
        setState { it.copy(cart = currentCart) }
    }

    private fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentCart = state.value.cart.toMutableList()
        val index = currentCart.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            currentCart[index] = currentCart[index].copy(quantity = quantity)
            setState { it.copy(cart = currentCart) }
        }
    }

    private fun removeFromCart(productId: String) {
        val currentCart = state.value.cart.toMutableList()
        currentCart.removeAll { it.productId == productId }
        setState { it.copy(cart = currentCart) }
    }

    private fun confirmSale(seller: String) {
        val current = state.value
        if (current.cart.isEmpty()) {
            emitEffect(PosUiEffect.ShowMessage("El carrito está vacío"))
            return
        }
        
        scope.launch {
            setState { it.copy(isSaving = true, errorMessage = null) }
            delay(800) // Animación premium de procesamiento
            
            val input = NewSaleInput(
                items = current.cart,
                paymentMethod = current.selectedPaymentMethod,
                globalDiscount = current.globalDiscount,
                sellerName = seller,
            )
            
            when (val result = addSaleUseCase(input)) {
                is AppResult.Success -> {
                    setState { 
                        it.copy(
                            isSaving = false, 
                            isCheckoutDialogVisible = false,
                            cart = emptyList(),
                            globalDiscount = 0.0,
                        ) 
                    }
                    emitEffect(PosUiEffect.SaleCompleted)
                    emitEffect(PosUiEffect.ShowMessage("Venta exitosa"))
                }
                is AppResult.Failure -> {
                    setState { it.copy(isSaving = false, errorMessage = result.error.message ?: "Error al confirmar venta") }
                }
            }
        }
    }
}
