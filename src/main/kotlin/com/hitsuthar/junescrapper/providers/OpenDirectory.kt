package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import org.jsoup.nodes.Document

fun getOpenDirectory(query: String, fetcher: DocumentFetcher): MovieLinksResponse {
    val formattedSearchQuery = query.trim().replace(" ", "+")
    val url = "https://filepursuit.com/pursuit?q=$formattedSearchQuery&type=video&sort=sizedesc"
    val document: Document = fetcher.fetchDocumentWithRetries(url)

    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()
    document.select("div.file-post-item").forEach { row ->
        val fileName = row.select("h5").text()
        val size = row.select("div span.bg-primary").last()?.text()?.replace(oldValue = " ", newValue = "")

        val copyLink = row.select("a").attr("onclick")
        val linkRegex = Regex("'([^']+)'")
        val link = linkRegex.find(copyLink)?.groupValues?.get(1)

        ddlStreams.add(DDLStream("${getNameAndQuality(fileName)} $size", link!!))
    }
    return MovieLinksResponse(ddlLinks = ddlStreams)
}