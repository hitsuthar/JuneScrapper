package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.rogMoviesUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun rogMoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$rogMoviesUrl/?s=$formattedQuery+"

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl =
            document.select("div.blog-pic a.blog-img").firstOrNull()?.attr("href")
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

fun getRogMovies(title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = rogMoviesSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
    document.select("div.entry-content > p > a").parallelStream().map { element ->
        val link = element.attr("href")
//        println(link)
        val nexDrive = Jsoup.parse(fetcher.fetchWithRetries(link)).select("p a").firstOrNull { element ->
            element.select("button").any { button ->
                button.text().contains("V-Cloud", ignoreCase = true)
            }
        }?.attr("href")
        if (nexDrive != null) {
            val vcloud = Jsoup.parse(fetcher.fetchWithRetries(nexDrive)).select("script:containsData(url)").html()
            val hubCloud = Regex("var url = '([^']*)'").find(vcloud)?.groupValues?.get(1)
            if (hubCloud != null) {
                val ddl = Extractor().extractFromHUbCloud(hubCloud, fetcher)
                if (ddl != null) {
                    ddlStreams.add(ddl)
                    println(ddl)
                }
            }
        }
    }.toList()

    return MovieLinksResponse(ddlStreams)
}