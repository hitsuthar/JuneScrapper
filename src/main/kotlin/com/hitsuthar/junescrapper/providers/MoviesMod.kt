package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveSeed
import com.hitsuthar.junescrapper.extractors.getUnblockedGames
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.moviesModUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun formattedQuery(query: String): String {
    return query.trim().replace(" ", "+")
}

fun moviesModSearch(query: String, fetcher: DocumentFetcher): List<String> {
    val searchUrl = "$moviesModUrl/search/${formattedQuery(query)}"
    val searchResults: MutableList<String> = emptyList<String>().toMutableList()
    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl = document.select("div.post-cards article > a").map {
            searchResults.add(it.attr("href"))
        }
        if (firstResultUrl.isEmpty()) emptyList() else searchResults
    } catch (e: Exception) {
        emptyList()
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun getMoviesMod(
    title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher
): MovieLinksResponse {
    try {
        val searchResultUrl = moviesModSearch(
            if (type == "movie") "${title.trim().replace(" ", "+")}+$year"
            else title.trim().replace(" ", "+"), fetcher
        )
        if (searchResultUrl.isEmpty()) {
            return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
        }
//        println(searchResultUrl)

        val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()


        searchResultUrl.map {
            if (type == "movie") {
                Jsoup.parse(fetcher.fetchWithRetries(it)).select("p a.maxbutton-1").parallelStream().map { element ->
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
            } else {
                Jsoup.parse(fetcher.fetchWithRetries(it)).select("div p a")
                    .filter { element ->
                        (element.parent()?.previousElementSibling()?.select("h3")?.text()
                            ?.contains("Season $season") == true || element.parent()?.previousElementSibling()
                            ?.select("h3")
                            ?.text()?.contains("Season$season") == true || element.parent()?.previousElementSibling()
                            ?.select("h3")?.text()?.contains("Season 0$season") == true || element.parent()
                            ?.previousElementSibling()?.select("h3")?.text()
                            ?.contains("Season0$season") == true) && element.text().contains("link", ignoreCase = true)
                    }
                    .parallelStream().map { element ->
                        val link = """[?&]url=([^&]+)""".toRegex().find(element.attr("href"))?.groupValues?.get(1)
                            ?.let { Base64.Default.decode(it).decodeToString() }
                        if (link != null) {
                            val ubGamesLink = Jsoup.parse(fetcher.fetchWithRetries(link)).select("h3 a")
                                .firstOrNull { it.text().contains("Episode $episode") }?.attr("href")
                            println("ubgamelink: " + ubGamesLink)
                            if (ubGamesLink != null) {
                                val driveSeedLink = getUnblockedGames(ubGamesLink, fetcher)
                                println("driveseed: " + driveSeedLink)
                                if (driveSeedLink.contains("driveseed", true)) {
                                    ddlStreams.add(getDriveSeed(driveSeedLink, fetcher))
                                }
                            }
                        }
                    }.toList()
            }

        }

        return MovieLinksResponse(ddlStreams)
    } catch (e: Exception) {
        println(e)
        return MovieLinksResponse(emptyList())
    }
}