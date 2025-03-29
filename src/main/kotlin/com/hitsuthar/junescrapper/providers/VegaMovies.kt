package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.extractFromHUbCloud
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.nodes.Document

fun vegaMoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://vegamovies.band"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery+"

    return try {
        val document: Document = fetcher.fetchDocumentWithRetries(searchUrl)
        val firstResultUrl =
            document.select("div.post-thumbnail a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
//            println(firstResultUrl)
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

fun getVegaMovies(title: String, year: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = vegaMoviesSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = fetcher.fetchDocumentWithRetries(searchResultUrl)
    document.select("div.entry-inner > p > a").parallelStream().map { element ->
        if (element.attr("href").contains("nexdrive")) {
            val link = element.attr("href")
//            println(link)
            val nexDrive = fetcher.fetchDocumentWithRetries(link).select("p a").firstOrNull { element ->
                element.select("button").any { button ->
                    button.text().contains("V-Cloud", ignoreCase = true)
                }
            }?.attr("href")
            if (nexDrive != null) {
                val vcloud = fetcher.fetchDocumentWithRetries(nexDrive).select("script:containsData(url)").html()
                val hubCloud = Regex("var url = '([^']*)'").find(vcloud)?.groupValues?.get(1)
                if (hubCloud != null) {
                    val ddl = extractFromHUbCloud(hubCloud, fetcher)
                    if (ddl != null) {
                        ddlStreams.add(ddl)
                        println(ddl)
                    }
                }
            }
        }
    }.toList()
    return MovieLinksResponse(ddlStreams)
}