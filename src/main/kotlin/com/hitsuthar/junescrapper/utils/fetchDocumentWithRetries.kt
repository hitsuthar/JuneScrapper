package com.hitsuthar.junescrapper.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun fetchDocumentWithRetries(
    url: String,
    timeout: Int = 120000,
): Document {
    return Jsoup.connect(url)
        .header(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.86 Safari/537.36"
        )
        .timeout(timeout)
        .get()

}