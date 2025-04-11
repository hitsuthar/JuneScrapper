package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup

fun moviesDriveSearch(query: String, fetcher: DocumentFetcher): List<String> {
    val baseUrl = "https://moviesdrive.guru"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery"

    val searchResults: MutableList<String> = emptyList<String>().toMutableList()

    return try {
        Jsoup.parse(fetcher.fetchWithRetries(searchUrl)).select("ul.recent-movies > li figcaption a").map {
            searchResults.add(it.attr("href"))
        }
        if (searchResults.isEmpty()) {
            emptyList()
        } else {
            searchResults
        }
    } catch (e: Exception) {
        emptyList()
    }
}


fun getMoviesDriveDDL(
    title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher
): MovieLinksResponse {
    try {
        val searchResultUrl = moviesDriveSearch(
            if (type == "movie") "${title.trim().replace(" ", "+")}+$year"
            else title.trim().replace(" ", "+"), fetcher
        )
        if (searchResultUrl.isEmpty()) {
            return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
        }
        val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()


        searchResultUrl.parallelStream().map {
//            println(it)
            if (type == "movie") {
                Jsoup.parse(fetcher.fetchWithRetries(it)).select("h5 a").parallelStream().map { element ->
                    val link = element.attr("href")

                    val ddlDocument = Jsoup.parse(fetcher.fetchWithRetries(link))

                    ddlStreams.add(
                        ddlDocument.select("a").firstOrNull { element ->
                            element.attr("href").contains("hubcloud", ignoreCase = true)
                        }?.attr("href")?.let { Extractor().getHubCloudUrl(it, fetcher) }!!
                    )
                }.toList()


            } else {
                Jsoup.parse(fetcher.fetchWithRetries(it)).select("a").filter { element ->
//                        println("filter: " + element.parent()?.select("span")?.text())

                    !element.attr("href").contains("zip", true) || element.attr("href")
                        .contains("episode-$episode", true) || element.parent()?.select("span")?.text()
                        ?.contains("ep", ignoreCase = true) == true
                    !element.attr("href").contains("All-Episode", true) && !element.text()
                        .contains("zip", ignoreCase = true)

                }.parallelStream().map { element ->
                    val link = element.attr("href")
                    if (link.contains("graph", true)) {
                        if ((link.contains("episode$episode", true) || link.contains(
                                "episode-$episode",
                                true
                            )) && (link.contains("season$season", true) || link.contains(
                                "season-$season", true
                            ) || link.contains("S-$season", true) || link.contains(
                                "S-0$season", true
                            ) || link.contains("S$season", true) || link.contains("S0$season", true))
                        ) {
                            println("graph: $link")
                            val hubCloud = Jsoup.parse(fetcher.fetchWithRetries(link)).select("a").firstOrNull {
                                it.attr("href").contains("hubcloud", ignoreCase = true)
                            }?.attr("href")
                            if (hubCloud != null) {
                                val ddl = Extractor().getHubCloudUrl(hubCloud, fetcher)
                                if (ddl != null) {
                                    ddlStreams.add(ddl)
                                }
                            }
                        }
                    }
                    if (link.contains("mdrive", true)) {

                        val ddlDocument = Jsoup.parse(fetcher.fetchWithRetries(link)).select("div.entry-content h5")
//                        println(ddlDocument.first()?.html())
                        if (ddlDocument.first()?.html()?.contains("Season $season") == true || ddlDocument.first()
                                ?.html()?.contains("Season-$season", true) == true
                        ) {
                            println("mdrive: $link")
                            val hubCloud = ddlDocument.firstOrNull { element ->
                                element.html()
                                    .contains("hubcloud", ignoreCase = true) && element.previousElementSibling()
                                    ?.text()
                                    ?.contains("ep", ignoreCase = true) == true && element.previousElementSibling()
                                    ?.select("span")?.first()?.text()
                                    ?.contains("$episode", ignoreCase = true) == true
                            }?.select("a")?.attr("href")
                            if (hubCloud != null) {
                                val ddl = Extractor().getHubCloudUrl(hubCloud, fetcher)
                                if (ddl != null) {
                                    ddlStreams.add(ddl)
                                }
                            }
                        }

                    }


                }.toList()
            }
        }.toList()
        return MovieLinksResponse(ddlLinks = ddlStreams)
    } catch (e: Exception) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
}
