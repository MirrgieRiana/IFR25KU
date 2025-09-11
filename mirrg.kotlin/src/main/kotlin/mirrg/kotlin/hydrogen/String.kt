package mirrg.kotlin.hydrogen

infix fun Byte.formatAs(format: String) = String.format(format, this)
infix fun Short.formatAs(format: String) = String.format(format, this)
infix fun Int.formatAs(format: String) = String.format(format, this)
infix fun Long.formatAs(format: String) = String.format(format, this)
infix fun Float.formatAs(format: String) = String.format(format, this)
infix fun Double.formatAs(format: String) = String.format(format, this)
