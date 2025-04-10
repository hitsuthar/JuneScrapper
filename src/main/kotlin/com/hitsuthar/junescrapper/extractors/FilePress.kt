package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.DocumentFetcher

fun getFilePress(url: String, fetcher: DocumentFetcher): DDLStream {
    println("url: $url")
    return DDLStream("", "")
}