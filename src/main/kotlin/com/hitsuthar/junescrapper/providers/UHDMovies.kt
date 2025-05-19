package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveLeech
import com.hitsuthar.junescrapper.extractors.getUnblockedGames
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.uHDMoviesUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

fun uHDMoviesSearch(query: String, fetcher: DocumentFetcher): List<String>? {
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$uHDMoviesUrl/search/$formattedQuery+"

    val searchResults: MutableList<String> = emptyList<String>().toMutableList()

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        document.select("article.gridlove-post div.entry-image a").map {
            searchResults.add(it.attr("href"))
        }
        println(searchResults)
        if (searchResults.isEmpty()) {
            null
        } else {
            searchResults
        }
    } catch (e: Exception) {
        null
    }
}

fun getUHDMovies(
    title: String,
    year: Int?,
    type: String?,
    season: Int?,
    episode: Int?,
    fetcher: DocumentFetcher
): MovieLinksResponse {

    val searchResultUrl = uHDMoviesSearch(
        if (type == "movie") "${title.trim().replace(" ", "+")}+$year"
        else title.trim().replace(" ", "+"), fetcher
    )
    if (searchResultUrl?.isEmpty() == true) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    searchResultUrl?.map {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(it))

        if (type == "movie") {
            document.select("div.entry-content > p a.maxbutton-1").reversed().parallelStream().map { element ->
                val link = element.attr("href")
                val ubGameLink = getUnblockedGames(link, fetcher)
                if (ubGameLink.contains("driveleech")) {
                    ddlStreams.add(getDriveLeech(ubGameLink, fetcher))
                }
            }.toList()
        } else if (type == "tv") {
            val episodesLink: MutableList<String> = emptyList<String>().toMutableList()
            document.select("div.entry-content h2").filter { element ->
                element.text().contains("Season") && element.text().contains(season.toString()) && !element.text()
                    .contains("-")
            }.parallelStream().map { element ->

                var currentElement = element.nextElementSibling()
                while (currentElement != null) {
                    // Stop if we reach next season or separator
                    if (currentElement.tagName() == "h2" ||
                        (currentElement.hasClass("mks_separator") && currentElement != element.nextElementSibling())
                    ) {
                        break
                    }

                    // Collect episode links
                    currentElement.select("a.maxbutton-gdrive-episode").filter { allButtons: Element ->
                        allButtons.text().contains("Ep", ignoreCase = true) && allButtons.text()
                            .contains("$episode")
                    }.map { episodelink->
                        episodesLink.add(episodelink.attr("href"))
                    }
//                        .parallelStream().map { button: Element ->
//                            val link = button.attr("href")
//                            val ubGameLink = getUnblockedGames(link, fetcher)
//                            if (ubGameLink.contains("driveleech")) {
//                                ddlStreams.add(getDriveLeech(ubGameLink, fetcher))
//                            }
//                        }.toList()

                    currentElement = currentElement.nextElementSibling()
                }
            }.toList()
            episodesLink
                .parallelStream().map {link->
                    val ubGameLink = getUnblockedGames(link, fetcher)
                    if (ubGameLink.contains("driveleech")) {
                        ddlStreams.add(getDriveLeech(ubGameLink, fetcher))
                    }
                }.toList()
        } else {
            MovieLinksResponse(ddlStreams)
        }
    }


    return MovieLinksResponse(ddlStreams)
}