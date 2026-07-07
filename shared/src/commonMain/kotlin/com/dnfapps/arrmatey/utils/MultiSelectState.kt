package com.dnfapps.arrmatey.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class MultiSelectState<T>(
    private val initialItems: Collection<T> = emptyList(),
    selectionModeAvailable: Boolean = true
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _selectedItems = MutableStateFlow<Set<T>>(initialItems.toSet())
    val selectedItems: StateFlow<Set<T>> = _selectedItems.asStateFlow()

    private val _isInSelectionMode = MutableStateFlow(false)
    val isInSelectionMode: StateFlow<Boolean> = _isInSelectionMode.asStateFlow()

    val selectionCount: StateFlow<Int> = _selectedItems
        .map { it.size }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isSelectionModeAvailable = MutableStateFlow(selectionModeAvailable)
    val isSelectionModeAvailable: StateFlow<Boolean> = _isSelectionModeAvailable.asStateFlow()

    fun setSelectionModeAvailable(value: Boolean) {
        _isSelectionModeAvailable.value = value
    }

    fun toggleSelectionModeAvailable() {
        _isSelectionModeAvailable.update { !it }
    }

    fun isSelected(item: T): Boolean = _selectedItems.value.contains(item)

    fun toggle(item: T) {
        if (!_isInSelectionMode.value && _isSelectionModeAvailable.value) {
            _isInSelectionMode.value = true
        }

        val currentItems = _selectedItems.value.toMutableSet()
        if (currentItems.contains(item)) {
            currentItems.remove(item)
        } else {
            currentItems.add(item)
        }
        _selectedItems.value = currentItems
    }

    fun toggleAll(items: Collection<T>) {
        if (!_isInSelectionMode.value && _isSelectionModeAvailable.value) {
            _isInSelectionMode.value = true
        }

        val currentItems = _selectedItems.value.toMutableSet()
        if (currentItems.containsAll(items)) {
            currentItems.removeAll(items.toSet())
        } else {
            currentItems.addAll(items)
        }
        _selectedItems.value = currentItems
    }

    fun select(item: T) {
        if (!_isInSelectionMode.value && _isSelectionModeAvailable.value) {
            _isInSelectionMode.value = true
        }

        _selectedItems.value += item
    }

    fun deselect(item: T) {
        _selectedItems.value -= item
    }

    fun selectAll(items: Collection<T>) {
        if (!_isInSelectionMode.value && _isSelectionModeAvailable.value) {
            _isInSelectionMode.value = true
        }

        _selectedItems.value += items
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
    }

    fun enterSelectionMode() {
        if (_isSelectionModeAvailable.value) {
            _isInSelectionMode.value = true
        }
    }

    fun exitSelectionMode() {
        _isInSelectionMode.value = false
        _selectedItems.value = emptySet()
    }

    fun areAllSelected(items: Collection<T>): Boolean {
        return _selectedItems.value.containsAll(items)
    }

}