package com.hitsuthar.junescrapper.utils

fun ExtractQuality(name: String): String {
    val regex = Regex("(\\d+(?:\\.\\d+)?(?:GB|MB)).*?(\\d+p|\\d+K)")
    val matchResult = regex.find(name)
//    if (matchResult != null) {
        val size = matchResult?.groupValues?.get(1)
        val quality = matchResult?.groupValues?.get(2)
        return "Size: $size, Quality: $quality"
//    } else return name

}