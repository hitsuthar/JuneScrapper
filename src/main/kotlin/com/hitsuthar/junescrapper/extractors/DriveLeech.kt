package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import org.jsoup.Jsoup

fun getDriveLeech(url: String): DDLStream {
    return try {
        val DL = Jsoup.connect(url.replace("file","zfile")).get()
        val title = DL.select("div > h5").text()
        val ddl = DL.select("a.btn").attr("href")
        println(getNameAndQuality(title))
        DDLStream(getNameAndQuality(title), ddl)
    } catch (e: Exception) {
        DDLStream(null.toString(), null.toString())
    }
}