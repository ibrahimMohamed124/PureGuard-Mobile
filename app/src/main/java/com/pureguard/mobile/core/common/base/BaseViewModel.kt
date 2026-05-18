package com.pureguard.mobile.core.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Event> : ViewModel() {
    private val _state = MutableStateFlow(initialState())
    val state = _state.asStateFlow()

    abstract fun initialState(): State
    abstract fun onEvent(event: Event)

    protected fun updateState(reducer: State.() -> State) {
        _state.update { it.reducer() }
    }
}