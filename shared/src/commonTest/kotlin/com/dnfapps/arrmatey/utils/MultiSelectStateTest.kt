package com.dnfapps.arrmatey.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MultiSelectStateTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialItemsAreSelected() {
        val state = MultiSelectState(initialItems = listOf("a", "b"))
        assertEquals(setOf("a", "b"), state.selectedItems.value)
        assertTrue(state.isSelected("a"))
        assertFalse(state.isInSelectionMode.value)
    }

    @Test
    fun testToggleAddsAndRemoves() {
        val state = MultiSelectState<String>()
        state.toggle("a")
        assertTrue(state.isSelected("a"))
        assertTrue(state.isInSelectionMode.value)

        state.toggle("a")
        assertFalse(state.isSelected("a"))
    }

    @Test
    fun testToggleEntersSelectionModeWhenAvailable() {
        val state = MultiSelectState<String>()
        assertFalse(state.isInSelectionMode.value)
        state.toggle("a")
        assertTrue(state.isInSelectionMode.value)
    }

    @Test
    fun testSelectionModeUnavailableBlocksAutoEnter() {
        val state = MultiSelectState<String>(selectionModeAvailable = false)
        state.toggle("a")
        assertTrue(state.isSelected("a"))
        assertFalse(state.isInSelectionMode.value)
    }

    @Test
    fun testEnterSelectionModeRespectsAvailability() {
        val state = MultiSelectState<String>(selectionModeAvailable = false)
        state.enterSelectionMode()
        assertFalse(state.isInSelectionMode.value)

        state.setSelectionModeAvailable(true)
        state.enterSelectionMode()
        assertTrue(state.isInSelectionMode.value)
    }

    @Test
    fun testExitSelectionModeClearsSelection() {
        val state = MultiSelectState<String>()
        state.select("a")
        state.select("b")
        assertTrue(state.isInSelectionMode.value)

        state.exitSelectionMode()
        assertFalse(state.isInSelectionMode.value)
        assertEquals(emptySet(), state.selectedItems.value)
    }

    @Test
    fun testClearSelectionEmptiesButKeepsMode() {
        val state = MultiSelectState<String>()
        state.select("a")
        state.clearSelection()
        assertEquals(emptySet(), state.selectedItems.value)
        // clearSelection does NOT flip out of selection mode
        assertTrue(state.isInSelectionMode.value)
    }

    @Test
    fun testToggleAllAddsWhenAnyMissing() {
        val state = MultiSelectState<String>()
        state.select("a")
        state.toggleAll(listOf("a", "b", "c"))
        assertEquals(setOf("a", "b", "c"), state.selectedItems.value)
    }

    @Test
    fun testToggleAllRemovesWhenAllPresent() {
        val state = MultiSelectState<String>()
        state.selectAll(listOf("a", "b", "c"))
        state.toggleAll(listOf("a", "b", "c"))
        assertEquals(emptySet(), state.selectedItems.value)
    }

    @Test
    fun testAreAllSelected() {
        val state = MultiSelectState<String>()
        state.selectAll(listOf("a", "b"))
        assertTrue(state.areAllSelected(listOf("a", "b")))
        assertFalse(state.areAllSelected(listOf("a", "b", "c")))
        assertTrue(state.areAllSelected(emptyList()))
    }

    @Test
    fun testDeselectDoesNotAffectOthers() {
        val state = MultiSelectState<String>()
        state.selectAll(listOf("a", "b", "c"))
        state.deselect("b")
        assertEquals(setOf("a", "c"), state.selectedItems.value)
    }

    @Test
    fun testToggleSelectionModeAvailable() {
        val state = MultiSelectState<String>(selectionModeAvailable = true)
        assertTrue(state.isSelectionModeAvailable.value)
        state.toggleSelectionModeAvailable()
        assertFalse(state.isSelectionModeAvailable.value)
        state.toggleSelectionModeAvailable()
        assertTrue(state.isSelectionModeAvailable.value)
    }
}
