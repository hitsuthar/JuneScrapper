package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun full4MoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://www.full4movies.casino"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery"
    println(searchUrl)

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl = document.select("div.posts-wrapper article a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

fun getFull4Movies(title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = full4MoviesSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
    document.select("div.wp-block-image > p a.fasc-button.fasc-size-large.dashicons-admin-links").parallelStream()
        .map { element ->
            val hubcloud = Jsoup.parse(fetcher.fetchWithRetries(element.attr("href"))).select("button[id=download-btn]")
                .firstOrNull { button ->
                    button.attr("onclick").contains("hubcloud", ignoreCase = true)
                }?.attr("onclick")?.let {
                    """window\.open\('([^']+)'""".toRegex().find(
                        it
                    )?.groupValues?.get(1)
                }
            if (hubcloud != null) {
                val ddl = Extractor().getHubCloudUrl(hubcloud, fetcher)
                if (ddl != null) ddlStreams.add(ddl)
            }
        }.toList()

    return MovieLinksResponse(ddlStreams)
}