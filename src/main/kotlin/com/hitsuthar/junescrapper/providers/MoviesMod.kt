package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveLeech
import com.hitsuthar.junescrapper.extractors.getDriveSeed
import com.hitsuthar.junescrapper.extractors.getUnblockedGames
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.fetchDocumentWithRetries
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun formattedQuery(query: String): String {
    return query.trim().replace(" ", "+")
}

fun moviesModSearch(query: String): String? {
    val baseUrl = "https://moviesmod.gift"
    val searchUrl = "$baseUrl/search/${formattedQuery(query)}"

    return try {
        val document: Document = Jsoup.connect(searchUrl).get()
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
fun getMoviesMod(title: String, year: Int?): MovieLinksResponse {
    val searchResultUrl = moviesModSearch("${title.trim().replace(" ", "+")}+$year")
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    println(searchResultUrl)

    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.connect(searchResultUrl).timeout(100000000).get()
    document.select("p a.maxbutton-1").mapNotNull { element ->
        val link = """[?&]url=([^&]+)""".toRegex().find(element.attr("href"))?.groupValues?.get(1)
            ?.let { Base64.Default.decode(it).decodeToString() }.toString()
//        println(link)

        val ddlDocument: Document = Jsoup.connect(link).timeout(10000000).get()
        val ubGamesLink = ddlDocument.select("a.maxbutton-fast-server-gdrive").attr("href")
        val newUrl = getUnblockedGames(ubGamesLink)
        if (newUrl.contains("driveseed")) {
//            println(newUrl)
            ddlStreams.add(getDriveSeed(newUrl))
        }
//        println(ubGamesLink)
    }
    return MovieLinksResponse(ddlStreams)
}