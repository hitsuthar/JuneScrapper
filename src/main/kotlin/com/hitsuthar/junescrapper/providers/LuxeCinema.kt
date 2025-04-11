package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.luxeCinemaUrl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Movie(
    val url: String,
    // Other fields can be added as needed
)



fun luxeMoviesSearch(query: String, fetcher: DocumentFetcher): String? {
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$luxeCinemaUrl/wp-json/dooplay/search/?keyword=$formattedQuery&nonce=13ada4c688"
    val json = fetcher.fetchWithRetries(searchUrl)
    println(json)

    val firstResult = Json.decodeFromString<Map<String, Movie>>(json).values.first()
    return firstResult.url
}

fun getLuxeCinema(
    title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher
): MovieLinksResponse {

    val searchResultUrl = luxeMoviesSearch(
        if (type == "movie") "${title.trim().replace(" ", "+")}+$year"
        else title.trim().replace(" ", "+"), fetcher
    )

    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()
    println(searchResultUrl)
    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))

    if (type == "movie") {
        document.select("div.custom-links > div a").parallelStream().map { element ->
            val link = element.attr("href")
            println(link)
            val hubCloud = Extractor().getHDMovie(link, fetcher).select("div.mirror-buttons a").firstOrNull { element ->
                element.text().contains("HubCloud", ignoreCase = true)
            }?.attr("href")
            if (hubCloud?.isNotBlank() == true) {
                ddlStreams.add(Extractor().getHubCloudUrl(hubCloud, fetcher)!!)
            } else null
        }.toList()
    } else {
        document.select("div.wp-content div").filter { element ->
            element.select("h3").text().contains("Season $season") || element.select("a span").text()
                .contains("Season $season", ignoreCase = true)

        }.parallelStream().map { element: Element ->
            val link = element.select("a").attr("href")
//            println(link)
            val extractedHDMovie = Extractor().getHDMovie(link, fetcher).select("div a").firstOrNull { element: Element ->
                val filteredEpisode = element.text().replace(Regex("""\s*\(.*?\)\s*"""), "")
//                println(filteredEpisode)
                filteredEpisode.contains("Ep", ignoreCase = true) && filteredEpisode.contains("$episode")
            }?.attr("href")
            println("extracted"+extractedHDMovie)
            if (extractedHDMovie?.contains("hubcloud",ignoreCase = true) == true) {
                val ddl = Extractor().getHubCloudUrl(extractedHDMovie, fetcher)
                if (ddl != null) ddlStreams.add(ddl) else {}
            } else null
        }.toList()

    }


    return MovieLinksResponse(ddlStreams)

}