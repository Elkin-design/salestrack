package org.salestrack.app.core.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.salestrack.app.core.dispatcher.DispatcherProvider

interface UiState
interface UiEvent
interface UiEffect

/**
 * ViewModel base para manejar estado y efectos de forma predecible y testeable.
 */
abstract class BaseViewModel<S : UiState, E : UiEvent, F : UiEffect>(
    initialState: S,
    private val dispatcherProvider: DispatcherProvider,
) {
    private val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatcherProvider.main)

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<F>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    protected fun setState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun emitEffect(effect: F) {
        scope.launch(dispatcherProvider.main) { _effects.emit(effect) }
    }

    abstract fun onEvent(event: E)

    open fun clear() {
        job.cancel()
    }
}

