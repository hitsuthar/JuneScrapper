package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveLeech
import com.hitsuthar.junescrapper.extractors.getUnblockedGames
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.nodes.Document

fun uHDMoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://uhdmovies.wales"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/search/$formattedQuery+"

    return try {
        val document: Document = fetcher.fetchDocumentWithRetries(searchUrl)
        val firstResultUrl =
            document.select("article.gridlove-post div.entry-image a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

fun getUHDMovies(title: String, year: Int?, fetcher: DocumentFetcher): MovieLinksResponse {

    val searchResultUrl = uHDMoviesSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = fetcher.fetchDocumentWithRetries(searchResultUrl)

    document.select("div.entry-content > p a.maxbutton-1").reversed().parallelStream().map { element ->
        val link = element.attr("href")
        val ubGameLink = getUnblockedGames(link, fetcher)
        if (ubGameLink.contains("driveleech")) {
            ddlStreams.add(getDriveLeech(ubGameLink, fetcher))
        }
    }.toList()

    return MovieLinksResponse(ddlStreams)
}