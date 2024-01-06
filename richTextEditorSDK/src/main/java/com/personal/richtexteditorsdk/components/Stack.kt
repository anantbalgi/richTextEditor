package com.personal.richtexteditorsdk.components

class Stack<T> {
    private val items = ArrayList<T>()

    // Push element onto the stack
    fun push(item: T) {
        items.add(item)
    }

    // Pop the top element from the stack
    fun pop(): T? {
        return if (isEmpty()) null else items.removeAt(items.size - 1)
    }

    // Peek at the top element without removing it
    fun peek(): T? = items.lastOrNull()

    // Check if the stack is empty
    fun isEmpty(): Boolean = items.isEmpty()

    // Get the size of the stack
    fun size(): Int = items.size

    fun forEachInReverse(action: (T) -> Unit) {
        for (i in items.size - 1 downTo 0) {
            action(items[i])
        }
    }

    fun contains(value: T): Boolean = items.contains(value)

    fun popElement(element: T): T? {
        val index = items.indexOf(element)
        return if (index != -1) items.removeAt(index) else null
    }

    fun values(): List<T> {
        val list = mutableListOf<T>()
        items.forEach { list.add(it) }
        return list
    }
}
