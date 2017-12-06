/**
 * MIT License
 *
 * Copyright (c) 2017 Curtis S. Cooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package us.csc.queens

import java.util.*

val EMPTY_BYTE_ARRAY = byteArrayOf()

/**
 * A fixed-sized stack of Byte integer values used by the N-queens solver.
 *
 * Written to avoid the overhead of boxing and any dynamic-memory
 * allocation that would be associated with a Collection.
 */
class FixedByteStack(val capacity: Int) {
    private val backingArray = ByteArray(capacity)
    private var currentIndex = -1

    fun empty(): Boolean = (currentIndex < 0)
    fun size(): Int = if (empty()) 0 else (currentIndex + 1)

    /** Does not mutate the stack: returns a copy. */
    fun toArray(): ByteArray {
        if (empty()) {
            return EMPTY_BYTE_ARRAY
        }

        val result = ByteArray(size())
        for (index in 0..currentIndex) {
            result[index] = backingArray[index]
        }

        return result
    }

    fun push(element: Byte) {
        val nextIndex = (currentIndex + 1)
        backingArray[nextIndex] = element
        currentIndex = nextIndex
    }

    fun top(): Byte {
        return backingArray[currentIndex]
    }

    fun pop(): Byte {
        val top = top()
        --currentIndex;
        return top
    }

    fun clear() {
        currentIndex = -1
        backingArray.fill(0)
    }

    override fun toString(): String {
        return "FixedByteStack" + Arrays.toString(toArray())
    }

    override fun hashCode(): Int {
        return toArray().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FixedByteStack) {
            return false
        }

        return (Arrays.equals(toArray(), other.toArray()))
    }
}
