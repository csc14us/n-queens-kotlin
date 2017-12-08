# n-queens-kotlin, v. 0.5.0
A [Kotlin](https://kotlinlang.org/) multi-core N-Queens solver with stack-based backtracking

This is my first foray into Kotlin after having read most of  
["Kotlin in Action"](https://www.amazon.com/Kotlin-Action-Dmitry-Jemerov/dp/1617293296),
as well as Jetbrains' thorough [documentation](https://kotlinlang.org/docs/reference/).  
I found the Kotlin language well-suited to the task at hand: find  
all solutions of the [N-Queens Puzzle](https://en.wikipedia.org/wiki/Eight_queens_puzzle)
for tractable board sizes,  
given the exponential complexity O(n!) of the back-tracking  
search algorithm used.  The Kotlin code was cleaner and more  
concise than the equivalent in Java without losing efficiency:  
It finds all [2,279,184 N-Queen solutions](https://oeis.org/A000170/list)
on a 15x15 chess board  
(single-core) in about 10 seconds on my home PC, which sports  
the impressive [Intel Core i7-4790K CPU @4GHz](https://ark.intel.com/products/80807/Intel-Core-i7-4790K-Processor-8M-Cache-up-to-4_40-GHz);
e.g.,

```
> java -jar bin\n-queens-0.5.0.jar --mode benchmark --size 15 --cores 1
Solving a 15x15 board; # threads = 1:

Time to solve: 9.86 [s]
Total solutions found = 2279184
```

A few notes on this implementation are in order.  First, it relies on  
a custom, fixed-sized stack instead of recursion for backtracking.  
This choice was made primarily for my own edification, since I had  
already read the recursive solution presented in Adam Drozdek's  
"Data Structures and Algorithms in C++ 3rd Edition"  
(ISBN-10: 0534491820).  I do not know if the stack-based approach  
is faster than a recursive implementation.  For details, I encourage  
readers to step through the code in *Solver.kt*, but here is an overview:

1. For each rank (possibly in parallel), put the first queen on the *a-file*.  
2. Iterate through the remaining files, placing queens on safe ranks.  
3. Placing queens entails pushing the rank onto the stack, whose size is the file.  
4. If a safe location for a queen is found on the last file, record the solution.  
5. Backtrack by popping queens off the stack to start an unexplored search avenue.
6. Repeat steps 2. to 5. until the last queen has been removed from the board.

Second, I note the current version provides several options users can  
experiment with, including the benchmark mode alluded to above  
to count all solutions for large boards, as well as a mode to print  
out a trace of each step as the search proceeds.  This can be helpful  
for visualizing how the back-tracking algorithm works in practice.  
More details can be obtained with using `--help` in the command-line:

```
> java -jar bin\n-queens-0.5.0.jar --help
* N-Queens, v. 0.5.0
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
  > java -jar bin/n-queens-0.5.0.jar --mode benchmark --size 14
  Counts all solutions to the N-Queens problem for a 14x14 chess board.

```

This program proved to be an enjoyable first project with Kotlin.  It  
provides a demonstration of the major syntactical features of the  
language, including its compatibility with the Java standard library,  
on a small but non-trivial problem rather than just snippets.  And it's  
a good measure of processor integer performance.  Enjoy!  And please  
feel free to comment and/or suggest improvements.  I prefer to stick  
with a stack-based solver and arrays for marking the board rather  
than bit twiddling for readability.  Otherwise, the implementation  
should not leave anything on the table performance-wise.



