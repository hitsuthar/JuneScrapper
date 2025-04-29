package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

@Serializable
private data class Response(
    val url: String
)

fun getDriveSeed(url: String, fetcher: DocumentFetcher): DDLStream {
    try {
        val DL = Jsoup.parse(fetcher.fetchWithRetries(url))
        if (DL.select("a.btn-warning").text().contains(" Resume Cloud ")) {
            println(DL.select("a.btn-warning").attr("href"))
        }
        val resumeBotUrl = DL.select("a.btn-light").attr("href")
//        val ddl = DL.select("a.btn").attr("href")
        println("resumeurl $resumeBotUrl")

        val resumeBotResponse = Jsoup.connect(resumeBotUrl).execute()
        val resumeBotDoc = resumeBotResponse.parse()

        val title = resumeBotDoc.select("div.card-header").text()

        val workerSeedUrl = resumeBotUrl.substringBefore("&do=")
        val token = """formData\.append\('token',\s*'([^']+)'\)""".toRegex()
            .find(resumeBotDoc.select("script:containsData(download)").html())?.groupValues?.get(1)
        val sessionID = resumeBotResponse.header("Set-Cookie")
            ?.let { """PHPSESSID=([^;]+)""".toRegex().find(it)?.groupValues?.get(1) }

//    println("sessionID: $sessionID")


        val client = OkHttpClient()
        val requestBody = token?.let {
            MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("token", it)
            .build()
        }

        val postRequest =
            requestBody?.let { Request.Builder().url(workerSeedUrl).post(it).addHeader("Cookie", "PHPSESSID=" + sessionID!!).build() }

//    var postResponse = Jsoup.connect(workerSeedUrl).method(Connection.Method.POST)
//        .cookie("PHPSESSID", sessionID!!).data(formData).execute()
        postRequest?.let { client.newCall(it).execute().use { response: okhttp3.Response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body?.string()
            val ddlUrl = Json.decodeFromString<Response>(responseBody!!).url
            return DDLStream(title, ddlUrl)

        } }


//
//        println(title)
//        DDLStream(null.toString(), null.toString())
        return DDLStream("", "")
    } catch (e: Exception) {
        println(e)
        return DDLStream("", "")
    }
}

