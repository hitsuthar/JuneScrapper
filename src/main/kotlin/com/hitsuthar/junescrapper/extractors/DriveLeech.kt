package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import org.jsoup.Jsoup

fun getDriveLeech(url: String, fetcher: DocumentFetcher): DDLStream {
    return try {
        val DL = fetcher.fetchDocumentWithRetries(url.replace("file", "zfile"))
        val title = DL.select("div > h5").text()
        val ddl = DL.select("a.btn").attr("href")
        println(title)
        DDLStream(title, ddl)
    } catch (e: Exception) {
        DDLStream(null.toString(), null.toString())
    }
}