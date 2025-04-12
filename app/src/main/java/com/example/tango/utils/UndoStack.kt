package com.example.tango.utils

class TangoAction(val oldValue: Int, val location: Pair<Int, Int>)
class QueensAction(val oldValue: Int, val location: Pair<Int, Int>)

class UndoStack<T>(
    val maxSize: Int = 1000
) {
    private val queue = ArrayDeque<T>()

    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    fun onAction(action: T) {
        queue.add(action)
        if (queue.size > maxSize) {
            queue.removeFirst()
        }
    }

    fun onUndo(): T? {
        return queue.removeLastOrNull()
    }

    fun clear() {
        queue.clear()
    }
}
