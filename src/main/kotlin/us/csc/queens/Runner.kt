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

private enum class ArgList(val cliArg: String) {
    CORES("--cores"),
    HELP("--help"),
    MODE("--mode"),
    SIZE("--size"),
    VERSION("--version");
}

private val USAGE: String = """* N-Queens, v. $NQUEENS_VERSION
Usage:
  --cores <#>: specify the number of CPU cores; must be >= 1; the default is 1 if not specified.
  --help: prints this help message and exits.
  --mode <MODE>: specify the program mode.  Choices for the MODE parameter include:
    benchmark - Count the solutions along with time spent; used to measure CPU performance.
    first - Print the first solution and exit; useful to see a solution of very large boards.
    trace: print a trace of the algorithm step-by-step, as well as the solutions.
    view: print all solutions of the N-Queen problem for the given board size.
  --size <#>: specify the size of the board; must be >= 1; the default is 8.
  --version: prints the version of the program and exits.

Example:
  > java -jar bin/n-queens-$NQUEENS_VERSION.jar --mode benchmark --size 14
  Counts all solutions to the N-Queens problem for a 14x14 chess board.
"""

fun main(args: Array<String>) {
    if (args.isEmpty() || !args.contains(ArgList.MODE.cliArg)) {
        println(USAGE)
        return
    }

    var boardSize: Byte = REGULAR_CHESS_BOARD_SIZE
    var numThreads = 1
    var traceSteps = false
    var printSolutions = false
    var stopAfterFirst = false

    var argIndex = 0
    while (argIndex < args.size) {
        val arg = args[argIndex].toLowerCase()
        when (arg) {
            ArgList.SIZE.cliArg -> try {
                boardSize = args[++argIndex].toByte()
            } catch (e: Throwable) {
                println(USAGE)
                return
            }

            ArgList.CORES.cliArg -> try {
                numThreads = args[++argIndex].toInt()
            } catch (e: Throwable) {
                println(USAGE)
                return
            }

            ArgList.MODE.cliArg -> try {
                val mode = args[++argIndex].toLowerCase()
                when (mode) {
                    "benchmark" -> printSolutions = false
                    "first" -> {
                        stopAfterFirst = true
                        printSolutions = true
                    }
                    "trace" -> {
                        traceSteps = true
                        printSolutions = true
                    }
                    "view" -> printSolutions = true
                    else -> {
                        println(USAGE)
                        return
                    }
                }
            } catch (e: Throwable) {
                println(USAGE)
                return
            }

            else -> when (arg) {
                ArgList.HELP.cliArg -> {
                    println(USAGE)
                    return
                }

                ArgList.VERSION.cliArg -> {
                    println("N-Queens, v. $NQUEENS_VERSION")
                    return
                }

                else -> {
                    println(USAGE)
                    return
                }
            }
        }
        ++argIndex
    }

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
    println(getTimeDateString(LocalDateTime.now()))
    println("Solving a ${boardSize}x${boardSize} board; # threads = $numThreads:")

    var solverResult = SolveResult()
    run {
        val startTime: Long = System.nanoTime()
        solverResult = solveNQ(
            boardSize = boardSize,
            numThreads = numThreads,
            printSteps = traceSteps,
            firstSolutionOnly = stopAfterFirst,
            collectSolutions = false,
            printSolutions = printSolutions)
        val endTime: Long = System.nanoTime()

        val duration: Double = (endTime - startTime).toDouble() / 1.0e9
        val durationStr = String.format("%.3g", duration)
        println("\nTime to solve: $durationStr [s]")
    }

    println("Total solutions found = ${solverResult.numSolutions}")
    println(getTimeDateString(LocalDateTime.now()))
}
 