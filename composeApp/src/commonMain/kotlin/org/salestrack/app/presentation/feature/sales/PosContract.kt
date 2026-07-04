package org.salestrack.app.presentation.feature.sales

import org.salestrack.app.core.presentation.UiEffect
import org.salestrack.app.core.presentation.UiEvent
import org.salestrack.app.core.presentation.UiState
import org.salestrack.app.domain.model.PaymentMethod
import org.salestrack.app.domain.model.Product
import org.salestrack.app.domain.model.SaleItem

data class PosUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = emptyList(),
    val inventoryProducts: List<Product> = emptyList(),
    val displayedProducts: List<Product> = emptyList(),
    val cart: List<SaleItem> = emptyList(),
    val globalDiscount: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isCheckoutDialogVisible: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val cartTotal: Double
        get() = (cart.sumOf { it.netTotal } - globalDiscount).coerceAtLeast(0.0)
    
    val cartSubtotal: Double
        get() = cart.sumOf { it.netTotal }
}

sealed interface PosUiEvent : UiEvent {
    data object Refresh : PosUiEvent
    data class QueryChanged(val value: String) : PosUiEvent
    data class CategoryChanged(val value: String?) : PosUiEvent
    data class AddToCart(val product: Product) : PosUiEvent
    data class UpdateCartItemQuantity(val productId: String, val quantity: Int) : PosUiEvent
    data class RemoveFromCart(val productId: String) : PosUiEvent
    data object ClearCart : PosUiEvent
    data class ToggleCheckoutDialog(val visible: Boolean) : PosUiEvent
    data class PaymentMethodChanged(val method: PaymentMethod) : PosUiEvent
    data class GlobalDiscountChanged(val discount: Double) : PosUiEvent
    data class ConfirmSale(val seller: String) : PosUiEvent
}

sealed interface PosUiEffect : UiEffect {
    data class ShowMessage(val message: String) : PosUiEffect
    data object SaleCompleted : PosUiEffect
}
