import java.io.File
import java.util.regex.Pattern

/**
 * Created by Simas on 2017 Sep 19.
 */
class Labyrinth {

  companion object {
    inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int) -> INNER): Array<Array<INNER>>
        = Array(sizeOuter) { Array(sizeInner, innerInit) }

    @JvmStatic
    fun main(args: Array<String>) {
      Labyrinth()
    }
  }

  /**
   * Count of operations performed while searching.
   */
  private var count = 0
  private var exists = false
  private val labyrinth: Array<Array<Int>>

  /**
   * Production step on the x axis (left, down, right, up).
   */
  private val cx = intArrayOf(-1, 0, 1, 0)
  /**
   * Production step on the y axis (left, down, right, up).
   */
  private val cy = intArrayOf(0, 1, 0, -1)

  init {
    // Input
    labyrinth = parseInput()

    val x: Int = readNum("Enter X:", "X must be within the labyrinth ([1..${labyrinth[0].size}])!") { x: Int ->
      x in 1..labyrinth[0].size
    }!!

    val y: Int = readNum("Enter Y:", "Y must be within the labyrinth ([1..${labyrinth.size}])!") { y: Int ->
      y in 1..labyrinth[0].size
    }!!

    // Initial step
    val l = 2

    println("1. DATA")
    println("1.1. Labyrinth")
    labyrinth[y-1][x-1] = l
    printArray2d(labyrinth)
    println("1.2. Initial position X=$x, Y=$y, L=$l")

    println("2. EXECUTION")
    // Internally we used indexes that start with 0, while externally (input/output) we start with 1.
    execute(l, x-1, y-1)

    println("3. RESULTS")
    if (exists) {
      println("3.1. Path found.")
      println("3.2. Path table")
      printArray2d(labyrinth)
    } else {
      println("3.1. Path not found.")
    }
  }

  private fun execute(l: Int, x: Int, y: Int) {
    if (x == 0 || y == 0 || x == labyrinth[0].size-1 || y == labyrinth.size-1) {
      println("Terminal.")
      exists = true
      return
    }

    var k = 0
    var step = l
    do {
      val u = x + cx[k]
      val v = y + cy[k]
      ++k
      print(String.format("%2d) %sR${k}. U=${u+1}, V=${v+1}, L=$step.", ++count, "-".repeated(step)))
      if (labyrinth[v][u] == 0) {
        ++step
        labyrinth[v][u] = step
        println(String.format(" Free. L = LAB[${u+1}, ${v+1}] = $step"))
        execute(step, u, v)
        if (!exists) {
          println(String.format("    %sBacktrack from X=${u+1}, Y=${v+1}. L = $step-1 = ${step - 1}. LAB[${u+1}, ${v+1}] = -1", "-".repeated(step)))
          labyrinth[v][u] = -1
          --step
        }
      } else if (labyrinth[v][u] == 1) {
        println(String.format(" Wall."))
      } else {
        println(String.format(" Thread."))
      }
    } while (!exists && k < 4)
  }

  private fun String.repeated(n: Int): String {
    return repeat(Math.max(n, 0))
  }

  private fun parseInput(): Array<Array<Int>> {
    // Open input file
    val input = File("input")
    if (!input.exists()) {
      throw IllegalStateException("Input file must exist!")
    }

    val rows = input.readLines()
    val rowCount = rows.size
    if (rowCount == 0) {
      throw IllegalStateException("Input file must contain rows separated by new lines!")
    }
    val colCount = rows[0].split(Pattern.compile("\\s")).size
    if (colCount == 0) {
      throw IllegalStateException("Input file must contain cols separated by white spaces!")
    }

    val array = array2d(rowCount, colCount) { 0 }

    rows.forEachIndexed { rowIndex, line ->
      // Get cols as strings
      val colStrings = line.split(Pattern.compile("\\s"))
      if (colStrings.size != colCount) {
        throw IllegalStateException("Input file rows must contain an equal amount of cols!")
      }

      // Parse cols into ints
      @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
      val colInts: Array<Int> = colStrings.map {
        val int = it.toIntOrNull()
        if (int == null) {
          throw IllegalStateException("Unexpected cell character found: '$it'! Must be either 0 or 1.")
        }
        int!!
      }.toTypedArray()

      // Input rows are numbered in a descending fashion so write to the last one first
      array[rowIndex] = colInts
    }

    return array
  }

  private fun printArray2d(array: Array<Array<Int>>) {
    if (array.isEmpty()) return

    println("Y, V ^")
    for (i in 0 until array.size) {
      print(String.format("  %2d | ", i+1))
      for (j in 0 until array[i].size) {
        print(String.format("%2d ", array[i][j]))
      }
      println()
    }
    print("     ")
    print("-".repeat(3 * array[0].size + 1))
    println("> X, U")
    print("       ")
    for (i in 0 until array[0].size) {
      print(String.format("%2d ", i+1))
    }
    println()
  }

  /**
   * Read a number from input.
   * @param prompt message shown before reading the user's input
   * @param validationError error shown when [validation] fails. %s may be used to be replaced as the actual line that was read.
   * @param retry if true will read input until a valid number is read.
   * * @param validation used to validate the entered line
   */
  private fun readNum(prompt: String, validationError: String, retry: Boolean = true, validation: (Int) -> Boolean): Int? {
    val retryTask = {
      if (retry) {
        readNum(prompt, validationError, retry, validation)
      } else {
        null
      }
    }

    // Print prompt
    println(prompt)

    // Read and validate line
    val line = readLine()
    val n = line?.toIntOrNull()
    if (n == null) {
      // Print coercion error and optionally retry
      println("'$line' could not be coerced into a number!")
      return retryTask.invoke()
    } else {
      if (validation.invoke(n)) {
        return n
      } else {
        // Print validation error and optionally retry
        println(String.format(validationError, line))
        return retryTask.invoke()
      }
    }
  }

}