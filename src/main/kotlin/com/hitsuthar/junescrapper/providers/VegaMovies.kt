package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getFilePress
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun vegaMoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://vegamovies.band"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery+"

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl = document.select("div.post-thumbnail a").firstOrNull()?.attr("href")
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

fun getVegaMovies(title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = vegaMoviesSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
    document.select("div.entry-inner > p > a").parallelStream().map { element ->
        try {
            if (element.attr("href").contains("nexdrive")) {
                val nexDrive = element.attr("href")
                val filePress = Jsoup.parse(fetcher.fetchWithRetries(nexDrive)).select("p a").firstOrNull { element ->
                    element.select("button").any { button ->
                        button.text().contains("Filepress", ignoreCase = true)
                    }
                }?.attr("href")
                if (filePress != null) {
                    getFilePress(filePress, fetcher)
                }
            }
        } catch (e: Exception) {
            println("VegaMovies: " + e.message)
        }
    }.toList()
    return MovieLinksResponse(ddlStreams)
}