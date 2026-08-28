package com.dnfapps.arrmatey.extensions

import com.dnfapps.arrmatey.compose.utils.SortOrder
import kotlin.test.Test
import kotlin.test.assertEquals

class ListExtensionsTest {

    private data class Item(val name: String, val value: Int?)

    private val items = listOf(
        Item("b", 2),
        Item("a", 3),
        Item("c", 1)
    )

    @Test
    fun testOrderedSortedByAsc() {
        val sorted = items.orderedSortedBy(SortOrder.Asc) { it.value }
        assertEquals(listOf(1, 2, 3), sorted.map { it.value })
    }

    @Test
    fun testOrderedSortedByDesc() {
        val sorted = items.orderedSortedBy(SortOrder.Desc) { it.value }
        assertEquals(listOf(3, 2, 1), sorted.map { it.value })
    }

    @Test
    fun testOrderedSortedByHandlesNullSelector() {
        val withNull = items + Item("d", null)
        val ascending = withNull.orderedSortedBy(SortOrder.Asc) { it.value }
        // sortedBy places nulls first
        assertEquals(null, ascending.first().value)
    }

    @Test
    fun testOrderedSortedByEmpty() {
        val empty = emptyList<Item>()
        assertEquals(empty, empty.orderedSortedBy(SortOrder.Asc) { it.value })
        assertEquals(empty, empty.orderedSortedBy(SortOrder.Desc) { it.value })
    }

    @Test
    fun testOrderedSortedWithAsc() {
        val comparator = compareBy<Item> { it.name }
        val sorted = items.orderedSortedWith(SortOrder.Asc, comparator)
        assertEquals(listOf("a", "b", "c"), sorted.map { it.name })
    }

    @Test
    fun testOrderedSortedWithDesc() {
        val comparator = compareBy<Item> { it.name }
        val sorted = items.orderedSortedWith(SortOrder.Desc, comparator)
        assertEquals(listOf("c", "b", "a"), sorted.map { it.name })
    }
}
