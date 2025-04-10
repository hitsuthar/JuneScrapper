package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun katMoviesHDSearch(query: String, fetcher: DocumentFetcher): String? {
    val baseUrl = "https://katmoviehd.rodeo"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/?s=$formattedQuery+"

    return try {
        val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchUrl))
        val firstResultUrl =
            document.select("div.post-content > h2 > a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            println(firstResultUrl)
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

fun getKatMoviesHD(title: String, year: Int?, fetcher: DocumentFetcher): MovieLinksResponse {
    val searchResultUrl = katMoviesHDSearch("${title.trim().replace(" ", "+")}+$year", fetcher)
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList())
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = Jsoup.parse(fetcher.fetchWithRetries(searchResultUrl))
    document.select("div.entry-content a").map { element ->
        if (element.attr("href").contains("kmhd") && element.attr("href").contains("/file/")) {
            val link = element.attr("href")
            println(link)
//            val nexDrive = fetcher.fetchDocumentWithRetries(link).select("p a").firstOrNull { element ->
//                element.select("button").any { button ->
//                    button.text().contains("V-Cloud", ignoreCase = true)
//                }
//            }?.attr("href")
//            if (nexDrive != null) {
//                val vcloud = fetcher.fetchDocumentWithRetries(nexDrive).select("script:containsData(url)").html()
//                val hubCloud = Regex("var url = '([^']*)'").find(vcloud)?.groupValues?.get(1)
//                if (hubCloud != null) {
//                    val ddl = extractFromHUbCloud(hubCloud, fetcher)
//                    if (ddl != null) {
//                        ddlStreams.add(ddl)
//                        println(ddl)
//                    }
//                }
//            }
        }
    }
    return MovieLinksResponse(ddlStreams)
}