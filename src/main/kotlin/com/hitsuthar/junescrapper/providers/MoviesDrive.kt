package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun moviesDriveSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://moviesdrive.guru"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery"

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl =
            document.select("ul.recent-movies > li figcaption a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}


fun getMoviesDriveDDL(title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    try {
        val formattedSearchQuery = title.trim().replace(" ", "+")
        val searchResultUrl = moviesDriveSearch("$formattedSearchQuery+$year", fetcher)
        if (searchResultUrl.isNullOrBlank()) {
            return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
        }
        val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
//        println(document)
        document.select("h5 > a").parallelStream().map { element ->
            val link = element.attr("href")
            println(link)
            val ddlDocument: Document = Jsoup.parse(fetcher.fetchWithRetries(link))
            println(ddlDocument)
            val relevantLinks = ddlDocument.select("a").filter { innerLink ->
                val href = innerLink.attr("href").lowercase()
                println(href)
                listOf("hubcloud").any { href.contains(it) }
            }
            relevantLinks.map { innerButton ->
                val source = innerButton.attr("href")
                ddlStreams.add(
                    Extractor().getHubCloudUrl(source, fetcher)!!
                )
            }
        }.toList()
        return MovieLinksResponse(ddlLinks = ddlStreams)
    } catch (e: Exception) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
}
