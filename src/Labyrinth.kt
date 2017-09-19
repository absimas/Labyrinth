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

  init {
    val array = parseInput()
    printArray2d(array)

//    val n: Int = readNum("Enter n:", "'%s' must be in bounds of [1..7]") { n: Int ->
//      n in 1..7
//    }!!
//    println("Read $n!")
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
      val colInts: Array<Int> = colStrings.map {
        val int = it.toIntOrNull()
        if (int == null || int !in 0..1 ) {
          throw IllegalStateException("Unexpected cell character found: '$it'! Must be either 0 or 1.")
        }
        int!!
      }.toTypedArray()

      // Input rows are numbered in a descending fashion so write to the last one first
      array[rowCount - rowIndex - 1] = colInts
    }

    return array
  }

  private fun printArray2d(array: Array<Array<Int>>) {
    if (array.isEmpty()) return

    println("Y, V ^")
    for (i in array.size-1 downTo 0) {
      print(String.format("  %2d | ", i+1))
      for (j in 0 until array.size) {
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