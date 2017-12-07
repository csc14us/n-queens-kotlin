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

import us.csc.queens.Board.Companion.toSquareId
import java.util.*
import kotlin.collections.ArrayList

const val REGULAR_CHESS_BOARD_SIZE: Byte = 8

/** Encodes the (rank, file) in a single value. */
internal typealias SquareId = Short

/**
 * Represents only the attacked squares of the board used to solve the N-Queens Problem.
 *
 * @param size The size N of the chess board; for a regular 8x8 board, use REGULAR_CHESS_BOARD_SIZE.
 */
internal class Board(val size: Byte) {
    internal companion object {
        fun toSquareId(boardSize: Byte, iFile: Byte, iRank: Byte): SquareId {
            return (iFile * boardSize + iRank).toShort()
        }

        fun toFileAndRankIndices(boardSize: Byte, squareId: SquareId): Pair<Byte, Byte> {
            val iFile = (squareId / boardSize)
            val iRank = (squareId - iFile * boardSize)

            return Pair(iFile.toByte(), iRank.toByte())
        }

        fun toSquareId(boardSize: Byte, square: Square): SquareId {
            val iRank = (square.rank - 1).toByte()
            val iFile = (square.file - 'a').toByte()

            return toSquareId(boardSize, iFile, iRank)
        }
    }

    private val ranksAttacked = BooleanArray(size.toInt(), { false })
    private val filesAttacked = BooleanArray(size.toInt(), { false })
    private val rightDiagonals = BooleanArray(2* size - 1, { false })
    private val leftDiagonals = BooleanArray(2* size - 1, { false })

    init {
        if (size < 1) {
            throw IllegalArgumentException("Board size = $size is invalid; must be >= 1.")
        }
    }

    fun clear() {
        val stateArrays = listOf(
            ranksAttacked, filesAttacked, rightDiagonals, leftDiagonals)
        for (arr in stateArrays) {
            arr.fill(false)
        }
    }

    fun addQueen(iFile: Byte, iRank: Byte) {
        markBoard(iFile, iRank, true)
    }

    fun addQueen(squareId: SquareId) {
        val pair = toFileAndRankIndices(size, squareId)
        addQueen(pair.first, pair.second)
    }

    /**
     * Important: this method is only correct if the caller knows no queens attack each other.
     * Verifying the precondition above is the caller's responsibility.
     */
    fun removeQueenFast(iFile: Byte, iRank: Byte) {
        markBoard(iFile, iRank, false)
    }

    fun isAttacked(iFile: Byte, iRank: Byte): Boolean {
        // Most likely reason the square is attacked is the row is occupied
        val row = iRank.toInt()
        if (ranksAttacked[row]) {
            return true
        }

        val col = iFile.toInt()
        val right: Int = toRightDiagonal(row, col)
        if (rightDiagonals[right]) {
            return true
        }

        val left: Int = toLeftDiagonal(row, col)
        if (leftDiagonals[left]) {
            return true
        }

        return filesAttacked[col]
    }

    fun getAttackedSquares(): Collection<SquareId> {
        val attackedSquares = HashSet<SquareId>()

        for (fileIdx: Int in 0 until size) {
            for (rankIdx in 0 until size) {
                val iFile = fileIdx.toByte()
                val iRank = rankIdx.toByte()
                if (isAttacked(iFile, iRank)) {
                    attackedSquares.add(toSquareId(size, iFile, iRank))
                }
            }
        }

        return attackedSquares
    }

    private fun markBoard(iFile: Byte, iRank: Byte, added: Boolean) {
        val row = iRank.toInt()
        ranksAttacked[row] = added

        val col = iFile.toInt()
        filesAttacked[col] = added

        val rightDiagonal: Int = toRightDiagonal(row, col)
        rightDiagonals[rightDiagonal] = added

        val leftDiagonal: Int = toLeftDiagonal(row, col)
        leftDiagonals[leftDiagonal] = added
    }

    private fun toRightDiagonal(row: Int, col: Int): Int {
        return (row - col + size - 1)
    }

    private fun toLeftDiagonal(row: Int, col: Int): Int {
        return (row + col)
    }
}

/** For testing the functionality of this file. */
internal fun main(args: Array<String>) {
    val regularBoard = Board(REGULAR_CHESS_BOARD_SIZE)

    val queenPositions = ArrayList<SquareId>()
    with(queenPositions) {
        add(Board.toSquareId(regularBoard.size, 0, 0))
        add(Board.toSquareId(regularBoard.size, 0, 6))
        add(Board.toSquareId(regularBoard.size, 7, 0))
        add(Board.toSquareId(regularBoard.size, 3, 4))
        add(Board.toSquareId(regularBoard.size, 2, 5))
        add(toSquareId(regularBoard.size, Square('e', 2)))
    }

    for (squareId: SquareId in queenPositions) {
        val fileAndRank = Board.toFileAndRankIndices(regularBoard.size, squareId)
        println("Queen: "+ Square(fileAndRank.first, fileAndRank.second))
        regularBoard.addQueen(squareId)
    }

    println()
    var attackedSquares: Collection<SquareId> = regularBoard.getAttackedSquares()
    printChessBoard(
        boardSize = regularBoard.size,
        attackedSquares = attackedSquares,
        queenPositions = queenPositions)

    println()
    regularBoard.clear()
    attackedSquares = regularBoard.getAttackedSquares()
    printChessBoard(
        boardSize = regularBoard.size,
        attackedSquares = attackedSquares,
        queenPositions = Collections.emptyList())
}
