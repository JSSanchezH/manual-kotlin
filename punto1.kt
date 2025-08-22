fun decimalToBinary(n: Int): String {
  if (n == 0) return "0"
  if (n == 1) return "1"
  return decimalToBinary(n / 2) + (n % 2).toString()
}

fun binaryToDecimal(bin: String): Int {
  fun helper(bin: String, index: Int): Int {
    if (index == bin.length) return 0
    val digit = bin[index].toString().toInt()
    val power = bin.length - index - 1
    return digit * Math.pow(2.0, power.toDouble()).toInt() + helper(bin, index + 1)
  }
  return helper(bin, 0)
}

// Ejemplo de uso
fun main() {
  val decimal = 27
  val binario = decimalToBinary(decimal)
  println("$decimal en binario es $binario")
  println("$binario en decimal es ${binaryToDecimal(binario)}")
}