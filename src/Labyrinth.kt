/**
 * Created by Simas on 2017 Sep 19.
 */
class Labyrinth {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Labyrinth()
    }
  }

  init {
    val n: Int = readNum("Enter n:", "'%s' must be in bounds of [1..7]") { n: Int ->
      n in 1..7
    }!!
    println("Read $n!")
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