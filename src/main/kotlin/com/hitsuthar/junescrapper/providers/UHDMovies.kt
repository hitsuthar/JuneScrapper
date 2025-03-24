package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.getDriveLeech
import com.hitsuthar.junescrapper.extractors.getHubCloudUrl
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.fetchDocumentWithRetries
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun uHDMoviesSearch(query: String): String? {
    val baseUrl = "https://uhdmovies.wales"
    val formattedQuery = query.trim().replace(" ", "+")
    val searchUrl = "$baseUrl/search/$formattedQuery+"

    return try {
        val document: Document = Jsoup.connect(searchUrl).get()
        val firstResultUrl =
            document.select("article.gridlove-post div.entry-image a").firstOrNull()?.attr("href")
        if (firstResultUrl.isNullOrBlank()) {
            null
        } else {
            firstResultUrl
        }
    } catch (e: Exception) {
        null
    }
}

fun getUHDMovies(title: String, year: Int?): MovieLinksResponse {

    val searchResultUrl = uHDMoviesSearch("${title.trim().replace(" ", "+")}+$year")
    if (searchResultUrl.isNullOrBlank()) {
        return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
    }
    val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

    val document: Document = fetchDocumentWithRetries(searchResultUrl)

    document.select("div.entry-content > p a.maxbutton-1").reversed().mapNotNull { element ->
        val link = element.attr("href")
//        println(link)
        val ddlDocument: Document = fetchDocumentWithRetries(link)
        val form = ddlDocument.select("form#landing")
        val formUrl = form.attr("action")
        val wpHttpName = form.select("input").attr("name")
        val wpHttp2Value = form.select("input").attr("value")
        val formData = mapOf(
            wpHttpName to wpHttp2Value,
        )
        val postResponse = Jsoup.connect(formUrl).method(Connection.Method.POST).data(formData).execute()
        val form2 = postResponse.parse().select("form#landing")
        val formUrl2 = form2.attr("action")
        val wpHttpValue2 = form2.select("input[name=_wp_http2]").attr("value")
        val tokenValue = form2.select("input[name=token]").attr("value")
        val formData2 = mapOf(
            "_wp_http2" to wpHttpValue2,
            "token" to tokenValue,
        )
//        println(form2)
        val postResponse2 = Jsoup.connect(formUrl2).method(Connection.Method.POST).data(formData2).execute()
        val jsCode = postResponse2.parse().select("script:containsData(verify_button)").html()

        val cookieMatch = """s_343\('([^']+)',\s*'([^']+)',\s*(\d+)\)""".toRegex().find(jsCode)
        val urlMatch = """setAttribute\(\s*["']href["']\s*,\s*["']([^"']+)["']\)""".toRegex().find(jsCode)

        if (cookieMatch != null && urlMatch != null) {
            val (cookieName, cookieValue, expiration) = cookieMatch.destructured
            val (url) = urlMatch.destructured

            val getResponse = Jsoup.connect(url).cookie(cookieName, cookieValue).get()
            val anotherUrl = """url=(https?://[^\s]+)""".toRegex()
                .find(getResponse.select("meta[http-equiv=refresh]").attr("content"))?.groupValues?.get(1)
            val getAnotherResponse = anotherUrl?.let { Jsoup.connect(it).get() }

            val replaceRegex = getAnotherResponse?.select("script")?.html()
                ?.let { """replace\("([^"]+)"""".toRegex().find(it) }?.groupValues?.get(1)

            val newUrl = replaceRegex?.let { anotherUrl.replace(Regex("""/r\?.*"""), it) }
            if (newUrl != null) {
                if (newUrl.contains("driveleech")) {
                    ddlStreams.add(getDriveLeech(newUrl))
                }
            }
        }


//        val relevantLinks = ddlDocument.select("a").filter { innerLink ->
//            val href = innerLink.attr("href").lowercase()
//            println(href)
//            listOf("hubcloud").any { href.contains(it) }
//        }
//        relevantLinks.mapNotNull { innerButton ->
//            val source = innerButton.attr("href")
//            ddlStreams.add(
//                getHubCloudUrl(source)
//            )
//        }
    }

    return MovieLinksResponse(ddlStreams)
}