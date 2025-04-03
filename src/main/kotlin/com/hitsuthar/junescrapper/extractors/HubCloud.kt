package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.getNameAndQuality

fun getHubCloudUrl(
    url: String,
    fetcher: DocumentFetcher
): DDLStream? {
    return try {
        val doc = fetcher.fetchDocumentWithRetries(url)
        val link = if (url.contains("drive")) {
            val scriptTag = doc.selectFirst("script:containsData(url)")?.toString() ?: ""
            Regex("var url = '([^']*)'").find(scriptTag)?.groupValues?.get(1) ?: ""
        } else doc.selectFirst("div.vd > center > a")?.attr("href") ?: ""
        extractFromHUbCloud(link, fetcher)
    } catch (e: Exception) {
        DDLStream(null.toString(), null.toString())
    }
}

fun extractFromHUbCloud(url: String, fetcher: DocumentFetcher): DDLStream? {
    val document = fetcher.fetchDocumentWithRetries(url)
    var ddl: String? = null
    val title = document.select("title").text()
    val size = document.select("li i[id=size]").text().replace(oldValue = " ", newValue = "")
    document.selectFirst("div.card-body")?.select("h2 a.btn")?.map { element ->
        val link1 = element.attr("href")
        val text = element.text()
        when {
            text.contains("Pixel", ignoreCase = true) -> ddl = link1
            text.contains("Download [FSL Server]") -> ddl = link1
            text.contains("Download File") -> ddl = link1
            text.contains("Download [Server : 1]", ignoreCase = true) -> ddl = link1
            else -> null
        }
    }
    return if (ddl != null) {
        DDLStream(getNameAndQuality(title) + " " + size, ddl!!)
    } else null
}