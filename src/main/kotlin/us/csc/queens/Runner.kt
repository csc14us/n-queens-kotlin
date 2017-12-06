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

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal val VERSION = "0.5.0"
private val PRINT_SOLUTIONS_THRESHOLD = REGULAR_CHESS_BOARD_SIZE

private enum class ArgList(val cliArg: String) {
    BOARD_SIZE("--board-size"),
    NUM_THREADS("--num-threads"),
    TRACE_STEPS("--trace-steps"),
    PRINT_SOLUTIONS("--print-solutions"),
    STOP_AFTER_FIRST("--stop-after-first");
}

private val USAGE: String = """* N-Queens, v. $VERSION
Usage:
  --board-size <#>: specify the size of the board; must be >= 1; the default is 8.
  --num-threads <#>: specify the number of threads; must be >= 1; the default is 1.
  --trace-steps: print a trace of the algorithm step-by-step.  Off by default.
  --print-solutions: print the solutions in addition to counting them.  Because
    printing is slow, the solutions are only printed by default for size
    ${PRINT_SOLUTIONS_THRESHOLD}x$PRINT_SOLUTIONS_THRESHOLD boards and smaller.
  --stop-after-first: stop after the first solution is found; by default, the
    program continues until all solutions are found.
"""

fun main(args: Array<String>) {
    var boardSize: Byte = REGULAR_CHESS_BOARD_SIZE
    var numThreads = 1
    var traceSteps = false
    var printSolutions = false
    var stopAfterFirst = false

    var argIndex = 0
    while (argIndex < args.size) {
        val arg = args[argIndex]
        when (arg) {
            ArgList.BOARD_SIZE.cliArg -> try {
                boardSize = args[++argIndex].toByte()
            } catch (e: Throwable) {
                println(USAGE)
                return
            }

            ArgList.NUM_THREADS.cliArg -> try {
                numThreads = args[++argIndex].toInt()
            } catch (e: Throwable) {
                println(USAGE)
                return
            }

            else -> when (arg) {
                ArgList.TRACE_STEPS.cliArg -> traceSteps = true
                ArgList.PRINT_SOLUTIONS.cliArg -> printSolutions = true
                ArgList.STOP_AFTER_FIRST.cliArg -> stopAfterFirst = true
                else -> {
                    println(USAGE)
                    return
                }
            }
        }
        ++argIndex
    }

    printSolutions = (printSolutions || boardSize <= PRINT_SOLUTIONS_THRESHOLD)
    runSolver(boardSize, numThreads, traceSteps, printSolutions, stopAfterFirst)
}

internal fun getTimeDateString(dateTime: LocalDateTime): String {
    return dateTime.format(DateTimeFormatter.ofPattern("MMM d yyyy hh:mm:ss a"))
}

private fun runSolver(boardSize: Byte,
                      numThreads: Int,
                      traceSteps: Boolean,
                      printSolutions: Boolean,
                      stopAfterFirst: Boolean) {
    val solver = Solver(boardSize, numThreads)

    println(getTimeDateString(LocalDateTime.now()))
    println("Solving a ${solver.boardSize}x${solver.boardSize} board with $numThreads threads:")

    var solverResult = Solver.Result()
    run {
        val startTime: Long = System.nanoTime()
        solverResult = solver.solve(
            printSteps = traceSteps,
            firstSolutionOnly = stopAfterFirst,
            collectSolutions = false,
            printSolutions = printSolutions)
        val endTime: Long = System.nanoTime()

        val duration: Double = (endTime - startTime).toDouble() / 1.0e9
        val durationStr = String.format("%.3g", duration)
        println("\nTime to solve: $durationStr [s]")
    }

    println("Total solutions = ${solverResult.numSolutions}")
    println(getTimeDateString(LocalDateTime.now()))
}
 