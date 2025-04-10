package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveSeed
import com.hitsuthar.junescrapper.extractors.getUnblockedGames
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun formattedQuery(query: String): String {
    return query.trim().replace(" ", "+")
}

fun moviesModSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://moviesmod.gift"
    val searchUrl = "$baseUrl/search/${formattedQuery(query)}"

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl =
            document.select("div.post-cards article a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun getMoviesMod(title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = moviesModSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    println(searchResultUrl)

    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
    document.select("p a.maxbutton-1").parallelStream().map { element ->
        val link = """[?&]url=([^&]+)""".toRegex().find(element.attr("href"))?.groupValues?.get(1)
            ?.let { Base64.Default.decode(it).decodeToString() }
//        println(link)

        val ddlDocument: Document = Jsoup.parse(fetcher.fetchWithRetries(link!!))
        val ubGamesLink = ddlDocument.select("a.maxbutton-fast-server-gdrive").attr("href")
        val newUrl = getUnblockedGames(ubGamesLink, fetcher)
        if (newUrl.contains("driveseed")) {
//            println(newUrl)
            ddlStreams.add(getDriveSeed(newUrl, fetcher))
        }
//        println(ubGamesLink)
    }.toList()
    return MovieLinksResponse(ddlStreams)
}