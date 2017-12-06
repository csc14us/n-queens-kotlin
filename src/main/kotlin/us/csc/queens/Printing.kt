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

const val EMPTY_SQUARE = '.'
const val ATTACKED_SQUARE = 'x'
const val QUEEN_SYMBOL = 'Q'

/** For printing squares using algebraic chess notation; e.g., "h1", "a3", "e5". */
data class Square(val file: Char, val rank: Byte) {
    /** Construct with indices from 0..N-1. */
    constructor(iFile: Byte, iRank: Byte): this('a' + iFile.toInt(), (iRank + 1).toByte())

    override fun toString(): String {
        return buildString {
            append(file).append(rank)
        }
    }
}

/**
 * Example output, in which empty squares are '.', attacked squares are 'x', and queens are 'Q':
 *
 * Board 8x8, Queens: 6
 * Attacked Squares: 60/64
 * 8|x x x x x . x x
 * 7|Q x x x x x x x
 * 6|x x Q x x x x x
 * 5|x x x Q x x x x
 * 4|x . x x x . x x
 * 3|x x x x x x . x
 * 2|x x x x Q x x x
 * 1|Q x x x x x x Q
 *   - - - - - - - -
 *   a b c d e f g h
 */
fun printChessBoard(boardSize: Byte,
                    attackedSquares: Collection<SquareId> = Collections.emptyList(),
                    queenPositions: Collection<SquareId> = Collections.emptyList())
{
    synchronized(printingLock, {
        printWithoutLock(queenPositions, boardSize, attackedSquares)
    })
}

private val printingLock = Any()

private fun printWithoutLock(queenPositions: Collection<SquareId>, boardSize: Byte, attackedSquares: Collection<SquareId>) {
    val queenCount: Int = queenPositions.size
    println("Board ${boardSize}x$boardSize, Queens: $queenCount")
    if (attackedSquares.isNotEmpty()) {
        println("Attacked Squares: ${attackedSquares.size}/${boardSize * boardSize}")
    }

    for (row: Int in 0 until boardSize) {
        print(String.format("%2d", boardSize - row))
        print('|')
        val iRank = (boardSize - 1 - row).toByte()
        for (col: Int in 0 until boardSize) {
            val iFile = col.toByte()
            val squareId: SquareId = Board.toSquareId(boardSize, iFile, iRank)
            when {
                queenPositions.contains(squareId) -> print(QUEEN_SYMBOL)
                attackedSquares.contains(squareId) -> print(ATTACKED_SQUARE)
                else -> print(EMPTY_SQUARE)
            }
            print(' ')
        }
        println()
    }

    print("   ")
    for (file: Int in 0 until boardSize) {
        print('-')
        print(' ')
    }

    println()
    print("   ")
    for (file: Int in 0 until boardSize) {
        val fileLetter: Char = 'a'.plus(file)
        print(fileLetter)
        print(' ')
    }

    println()
}
