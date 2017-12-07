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

import com.google.common.util.concurrent.MoreExecutors
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList

typealias SquareIdArray = ShortArray
typealias Solution = SquareIdArray

val NQUEENS_VERSION = "0.5.0"
val EMPTY_SQUARE_ID_ARRAY = SquareIdArray(0)

/** Return type of solveNQ.  Use combineResults to combine two instances. */
data class SolveResult(val numSolutions: Long, val solutionList: List<Solution>) {
    constructor(): this(0, ArrayList())

    companion object {
        fun combineResults(lhs: SolveResult, rhs: SolveResult): SolveResult {
            val numSolutions: Long = (lhs.numSolutions + rhs.numSolutions)

            val solutionList: List<Solution>
            when {
                numSolutions > 0 -> {
                    solutionList = ArrayList(numSolutions.toInt())
                    solutionList.addAll(lhs.solutionList)
                    solutionList.addAll(rhs.solutionList)
                }

                else -> {
                    solutionList  = Collections.emptyList()
                }
            }

            return SolveResult(numSolutions, solutionList)
        }
    }
}

/**
 * Solve the N-queens problem for the given board.
 *
 * This function can be called multiple times safely.
 *
 * @param boardSize Size of the chess board to solve for; must be >= 1.
 * @param numThreads Number of threads to use, which must be >= 1.
 * @param printSteps If true, print the steps; i.e., the intermediate
 *  positions attained during the search.
 * @param firstSolutionOnly If true, stop the search after a solution
 *  has been found and verified.
 * @param collectSolutions If true, collect the solutionList and return
 *  them in a List in the order in which they were found.
 * @param printSolutions If true, the board will be printed every time
 *  a solution is discovered.  Only use for small board sizes.
 * @return Number of solutions found, as well as a list of the solutions
 *  if collectSolutions is true or an empty list otherwise.
 */
fun solveNQ(boardSize: Byte,
            numThreads: Int,
            printSteps: Boolean,
            firstSolutionOnly: Boolean,
            collectSolutions: Boolean,
            printSolutions: Boolean): SolveResult
{
    if (firstSolutionOnly) {
        return findFirstSolution(boardSize, printSteps)
    }

    val executorService: ExecutorService = when {
        numThreads < 1 -> throw IllegalArgumentException("numThreads = $numThreads is invalid; must be >= 1")
        numThreads == 1 -> MoreExecutors.newDirectExecutorService()
        else -> {
            val threadPoolSize = Math.min(boardSize.toInt(), numThreads)
            Executors.newFixedThreadPool(threadPoolSize)
        }
    }

    try {
        return solveNQWithExecutor(
            boardSize,
            executorService,
            printSteps,
            collectSolutions,
            printSolutions)
    } finally {
        executorService.shutdown()
    }
}

private fun solveNQWithExecutor(boardSize: Byte,
                                executorService: ExecutorService,
                                printSteps: Boolean,
                                collectSolutions: Boolean,
                                printSolutions: Boolean): SolveResult {
    val totalSolutionCount = AtomicLong(0)
    val solverList: List<Solver> = List(boardSize.toInt(), { Solver(boardSize, totalSolutionCount) })

    val futures = ArrayList<Future<SolveResult>>()
    for (row in 0 until boardSize) {
        val solver = solverList[row]
        val future: Future<SolveResult> = executorService.submit(Callable<SolveResult> {
            return@Callable solver.solveWithFirstQueenPlaced(
                rankOfFirstQueen = row.toByte(),
                printSteps = printSteps,
                collectSolutions = collectSolutions,
                printSolutions = printSolutions,
                firstSolutionOnly = false)
        })

        futures.add(future)
    }

    var finalResult = SolveResult()
    for (future in futures) {
        finalResult = SolveResult.combineResults(finalResult, future.get())
    }

    return finalResult
}

private fun findFirstSolution(boardSize: Byte, printSteps: Boolean): SolveResult {
    var row = 0
    val totalSolutionCount = AtomicLong(0)
    while (row < boardSize) {
        val solver = Solver(boardSize, totalSolutionCount)
        val solveResult = solver.solveWithFirstQueenPlaced(row.toByte(), printSteps, true, true, true)
        if (solveResult.numSolutions > 0) {
            return solveResult
        }
        ++row
    }

    return SolveResult()
}

/**
 * A stack-based backtracking solver for the N-Queens problem.
 *
 * The problem is described in detail here:
 * https://en.wikipedia.org/wiki/Eight_queens_puzzle.
 */
private class Solver(boardSize: Byte, private var totalSolutionCount: AtomicLong) {
    private val board: Board = Board(boardSize)
    private val queenPositions = FixedByteStack(board.size.toInt())

    /**
     * @param rankOfFirstQueen The first queen (on the 'a' file) will appear on this rank.
     */
    fun solveWithFirstQueenPlaced(rankOfFirstQueen: Byte,
                                  printSteps: Boolean,
                                  collectSolutions: Boolean,
                                  printSolutions: Boolean,
                                  firstSolutionOnly: Boolean): SolveResult {
        addQueen(iFile = 0, iRank = rankOfFirstQueen, printSteps = printSteps)
        if (printSteps) {
            printCurrentBoard(showAttackedSquares = true)
        }

        var numSolutions: Long = 0
        val solutions = ArrayList<Solution>()
        while (!queenPositions.empty()) {
            placeQueens(printSteps)

            // A solution has been found all files/columns are filled
            val solutionFound = (board.size == queenPositions.size().toByte())
            if (solutionFound) {
                ++numSolutions
                if (collectSolutions) {
                    solutions.add(toSquareIds())
                }


                if (printSolutions) {
                    synchronized(totalSolutionCount, {
                        totalSolutionCount.getAndIncrement()
                        println("Solution $totalSolutionCount:\n")
                        printCurrentBoard(showAttackedSquares =  false)
                    })
                }

                if (firstSolutionOnly) {
                    break
                }
            }

            backtrack(printSteps)
            if (queenPositions.size() == 1) {
                break
            }
        }

        return SolveResult(numSolutions, solutions)
    }

    /** Place queens on consecutive files until there's no more room to place more. */
    private fun placeQueens(printSteps: Boolean) {
        for (col: Int in queenPositions.size() until board.size) {
            // Find a safe spot in the file to place the next queen
            var safeSquareFound = false
            val iFile = col.toByte()
            for (row: Int in 0 until board.size) {
                val iRank = row.toByte()
                if (!board.isAttacked(iFile, iRank)) {
                    addQueen(iFile, iRank, printSteps)
                    safeSquareFound = true
                    break
                }
            }

            // Stuck: couldn't place a queen on the current file
            if (!safeSquareFound) {
                break
            }
        }
    }

    /** Backtrack to the next viable search avenue by popping queens off the stack. */
    private fun backtrack(printSteps: Boolean) {
        while (!queenPositions.empty()) {
            // Remove queen from the last file
            val col: Int = (queenPositions.size() - 1)
            val iFile = col.toByte()
            var iRank: Byte = queenPositions.top()
            removeLastQueen(iFile, iRank, printSteps)

            // Finished backtracking when a safe position in the file is found
            // in which to start forward searching from with placeQueens().
            while (++iRank < board.size) {
                if (!board.isAttacked(iFile, iRank)) {
                    addQueen(iFile, iRank, printSteps)
                    return
                }
            }
        }
    }

    private fun addQueen(iFile: Byte, iRank: Byte, printSteps: Boolean) {
        if (printSteps) {
            printlnWithLock("\nAdding queen to ${Square(iFile, iRank)}")
        }

        board.addQueen(iFile, iRank)
        queenPositions.push(iRank)

        if (printSteps) {
            printCurrentBoard(showAttackedSquares =  true)
        }
    }

    /**
     * Pop the last-placed queen from the board, which pops the stack.
     */
    private fun removeLastQueen(iFile: Byte, iRank: Byte, printSteps: Boolean) {
        if (printSteps) {
            printlnWithLock("\nRemoving queen from ${Square(iFile, iRank)}")
        }

        // Calling removeQueenFast is safe here because the algorithm prevents
        // queens from ever attacking each other during its execution.
        board.removeQueenFast(iFile, iRank)
        queenPositions.pop()

        if (printSteps) {
            printCurrentBoard(showAttackedSquares =  true)
        }
    }

    private fun printCurrentBoard(showAttackedSquares: Boolean = true) {
        val squaresWithQueens: SquareIdArray = toSquareIds()
        println()
        val attackedSquares: Collection<SquareId> =
            if (showAttackedSquares) board.getAttackedSquares() else Collections.emptyList()
        printChessBoard(board.size, attackedSquares, squaresWithQueens.toList())
        println()
    }

    private fun toSquareIds(): SquareIdArray {
        if (queenPositions.empty()) {
            return EMPTY_SQUARE_ID_ARRAY
        }

        val squareIds = SquareIdArray(queenPositions.size())
        for ((col, iRank) in queenPositions.toArray().withIndex()) {
            squareIds[col] = Board.toSquareId(board.size, iFile = col.toByte(), iRank = iRank)
        }

        return squareIds
    }
}