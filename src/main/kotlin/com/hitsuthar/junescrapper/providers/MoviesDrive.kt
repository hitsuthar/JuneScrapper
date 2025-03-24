package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getHubCloudUrl
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.fetchDocumentWithRetries
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun moviesDriveSearch(query: String): String? {
    val baseUrl = "https://moviesdrive.guru"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery"

    return try {
        val document: Document = fetchDocumentWithRetries(searchUrl)
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


fun getMoviesDriveDDL(title: String, year: Int?): MovieLinksResponse {
    try {


        val formattedSearchQuery = title.trim().replace(" ", "+")
        val searchResultUrl = moviesDriveSearch("$formattedSearchQuery+$year")
        if (searchResultUrl.isNullOrBlank()) {
            return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
        }
        val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

        val document: Document = fetchDocumentWithRetries(searchResultUrl)
//        println(document)
        document.select("h5 > a").mapNotNull { element ->
            val link = element.attr("href")
            println(link)
            val ddlDocument: Document = fetchDocumentWithRetries(link)
            println(ddlDocument)
            val relevantLinks = ddlDocument.select("a").filter { innerLink ->
                val href = innerLink.attr("href").lowercase()
                println(href)
                listOf("hubcloud").any { href.contains(it) }
            }
            relevantLinks.mapNotNull { innerButton ->
                val source = innerButton.attr("href")
                ddlStreams.add(
                    getHubCloudUrl(source)
                )
            }
        }
        return MovieLinksResponse(ddlLinks = ddlStreams)
    } catch (e: Exception) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
}
