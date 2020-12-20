package site.liangbai.lrainylib.util

fun String.isEmpty(): Boolean {
    return this == ""
}

fun Array<String>.isEmpty(): Boolean {
    return size <= 0 || this[0] == ""
}