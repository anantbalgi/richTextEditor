package com.personal.richtexteditorsdk.datastructures

/**
 * A generic stack implementation in Kotlin.
 *
 * @param T The type of elements stored in the stack.
 */
class Stack<T> {

    // Internal storage for stack elements
    private val items = ArrayList<T>()

    /**
     * Pushes an element onto the stack.
     *
     * @param item The element to be pushed onto the stack.
     */
    fun push(item: T) {
        items.add(item)
    }

    /**
     * Pops the top element from the stack.
     *
     * @return The popped element, or null if the stack is empty.
     */
    fun pop(): T? {
        return if (isEmpty()) null else items.removeAt(items.size - 1)
    }

    /**
     * Peeks at the top element without removing it.
     *
     * @return The top element, or null if the stack is empty.
     */
    fun peek(): T? = items.lastOrNull()

    /**
     * Checks if the stack is empty.
     *
     * @return True if the stack is empty, false otherwise.
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * Gets the size of the stack.
     *
     * @return The number of elements in the stack.
     */
    fun size(): Int = items.size

    /**
     * Performs the given action on each element of the stack in reverse order.
     *
     * @param action The action to be performed on each element.
     */
    fun forEachInReverse(action: (T) -> Unit) {
        for (i in items.size - 1 downTo 0) {
            action(items[i])
        }
    }

    /**
     * Checks if the stack contains the specified element.
     *
     * @param value The element to be checked for presence in the stack.
     * @return True if the element is present, false otherwise.
     */
    fun contains(value: T): Boolean = items.contains(value)

    /**
     * Pops the specified element from the stack.
     *
     * @param element The element to be popped from the stack.
     * @return The popped element, or null if the element is not present in the stack.
     */
    fun popElement(element: T): T? {
        val index = items.indexOf(element)
        return if (index != -1) items.removeAt(index) else null
    }

    /**
     * Returns a list containing all elements in the stack.
     *
     * @return A list of stack elements.
     */
    fun values(): List<T> {
        val list = mutableListOf<T>()
        items.forEach { list.add(it) }
        return list
    }
}
