package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup

fun getDriveLeech(url: String, fetcher: DocumentFetcher): DDLStream {
    return try {
        val DL = Jsoup.parse(fetcher.fetchWithRetries(url.replace("file", "zfile")))
        val title = DL.select("div > h5").text()
        val ddl = DL.select("a.btn").attr("href")
        println(title)
        DDLStream(title, ddl)
    } catch (e: Exception) {
        DDLStream(null.toString(), null.toString())
    }
}