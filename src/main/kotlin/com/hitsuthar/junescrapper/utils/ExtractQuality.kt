package com.hitsuthar.junescrapper.utils

fun getNameAndQuality(name: String): String {
    val regex = Regex("(.*?)\\.(\\b\\d{3,4}p\\b|\\b4K\\b)\\..*")
    val matchResult = regex.find(name)
    return if (matchResult != null) {
        val movieName = matchResult.groupValues[1]
        val quality = matchResult.groupValues[2]
        return "$movieName $quality"
    } else name
}