import java.io.File
import java.util.regex.Pattern

/**
 * Created by Simas on 2017 Oct 9.
 */
private const val INITIAL_WAVE = 2
class LabyrinthWave {

  companion object {
    inline fun <reified INNER> array2d(sizeOuter: Int, sizeInner: Int, noinline innerInit: (Int) -> INNER): Array<Array<INNER>>
        = Array(sizeOuter) { Array(sizeInner, innerInit) }

    @JvmStatic
    fun main(args: Array<String>) {
      LabyrinthWave()
    }
  }

  /**
   * Vertices affected by each wave.
   */
  private val f = arrayListOf<Pair<Int, Int>>()
  /**
   * Production steps. First - x axis, second - y axis.
   */
  private val prod = arrayOf(Pair(-1, 0), Pair(0, -1), Pair(1, 0), Pair(0, 1))
  private val labyrinth: Array<Array<Int>>
  private val rules = mutableListOf<String>()
  private val vertices = mutableListOf<String>()
  /**
   * Currently executed wave index.
   */
  private var wave = 0
  /**
   * How many vertexes are affected by the current wave.
   */
  private var waveVertexCount = 1
  /**
   * Used as a logging requirement only.
   */
  private var new = 1
  /**
   * Used as a logging requirement only.
   */
  private var closing = 1

  init {
    // Input
    labyrinth = parseInput()

//    val x: Int = readNum("Enter X:", "X must be within the labyrinth ([1..${labyrinth[0].size}])!") { x: Int ->
//      x in 1..labyrinth[0].size
//    }!!
//
//    val y: Int = readNum("Enter Y:", "Y must be within the labyrinth ([1..${labyrinth.size}])!") { y: Int ->
//      y in 1..labyrinth[0].size
//    }!!
    val x = 5
    val y = 4

    println("1. DATA")
    println("1.1. Labyrinth")
    f.add(Pair(x-1, y-1))
    labyrinth[y-1][x-1] = INITIAL_WAVE
    printArray2d(labyrinth)
    println("1.2. Initial position X=$x, Y=$y, L=$INITIAL_WAVE")

    println("2. EXECUTION")
    println("WAVE ${wave++}, L=\"2\". Initial position X=$x, Y=$y, NEW=${new++}")
    val finalCoordinates = execute(0)

    println("3. RESULTS")
    if (finalCoordinates != null) {
      println("3.1. Path found.")
      println("3.2. Path table")
      printArray2d(labyrinth)

      // Trace back to find path rules and vertices
      traceBack(finalCoordinates.first, finalCoordinates.second)

      println("3.3. Path rules")
      println("${rules.toString().removeSurrounding("[","]")}.")
      println("3.4. Path vertices")
      println("${vertices.toString().removeSurrounding("[","]")}.")
    } else {
      println("3.1. Path not found.")
    }
  }

  /**
   * Recursive wave (depth) search. Starts from the first vertex saved in [f].
   * @param l last checked vertex from [f]
   */
  private fun execute(l: Int): Pair<Int, Int>? {
    if (l > f.size) return null

    val x = f[l].first
    val y = f[l].second

    if (wave != labyrinth[y][x]-1) {
      wave = labyrinth[y][x] - 1
      println("WAVE ${wave}, L=\"${wave+2}\"")
    }

    if (isTerminal(x, y)) return null
    println(" Closing UZD=${closing++}, X=${x+1}, Y=${y+1}.")

    for (k in 0 until 4) {
      val u = x + prod[k].first
      val v = y + prod[k].second
      print("   R${k+1}. X=${u+1}, Y=${v+1}.")

      if (labyrinth[v][u] == 0) {
        labyrinth[v][u] = labyrinth[y][x] + 1
        print(" Free. NEW=${new++}.")
        if (isTerminal(u, v)) {
          println(" Terminal.")
          return Pair(u, v)
        } else {
          f += Pair(u, v)
        }
      } else if (labyrinth[v][u] == 1) {
        print(" Wall.")
      } else {
        print(" CLOSED or OPENED.")
      }
      println()
    }

    return execute(l+1)
  }

  /**
   * Trace back from the exit point (x,y) to the beginning.
   * @param x x coordinate (starts with 0)
   * @param y y coordinate (starts with 0)
   */
  private fun traceBack(x: Int, y: Int) {
    val wave = labyrinth[y][x]
    vertices.add(0, "[X=${x+1},Y=${y+1}]")
    if (wave == INITIAL_WAVE) return

    for (k in 3 downTo 0) {
      val u = x + prod[k].first
      val v = y + prod[k].second

      if (!isTerminal(u, v) && labyrinth[v][u] == wave - 1) {
        val kInverted = ((k+2) % 4) + 1
        rules.add(0, "R$kInverted")
        traceBack(u, v)
        return
      }
    }
  }

  @Suppress("ReplaceRangeToWithUntil")
  private fun isTerminal(x: Int, y: Int): Boolean {
    return x !in 1..labyrinth[0].size-2 || y !in 1..labyrinth.size-2
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
      array[rowCount - rowIndex - 1] = colInts
    }

    return array
  }

  private fun printArray2d(array: Array<Array<Int>>) {
    if (array.isEmpty()) return

    println("Y, V ^")
    for (i in array.size-1 downTo 0) {
      print(String.format("  %3d | ", i+1))
      for (j in 0 until array[i].size) {
        print(String.format("%3d ", array[i][j]))
      }
      println()
    }
    print("      ")
    print("-".repeat(4 * array[0].size + 1))
    println("> X, U")
    print("        ")
    for (i in 0 until array[0].size) {
      print(String.format("%3d ", i+1))
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