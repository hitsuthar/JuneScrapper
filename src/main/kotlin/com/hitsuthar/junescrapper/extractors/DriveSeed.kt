package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup

fun getDriveSeed(url: String, fetcher: DocumentFetcher): DDLStream {
//        println("hehe")
    val DL = Jsoup.parse(fetcher.fetchWithRetries(url))
    if (DL.select("a.btn-warning").text().contains(" Resume Cloud ")) {
        println(DL.select("a.btn-warning").attr("href"))
    }
    val resumeBotUrl = DL.select("a.btn-light").attr("href")
//        val ddl = DL.select("a.btn").attr("href")
//    println("resumeurl $resumeBotUrl")
    val resumeBotResponse = Jsoup.connect(resumeBotUrl).execute()
    val resumeBotDoc = resumeBotResponse.parse()

    val workerSeedUrl = resumeBotUrl.substringBefore("&do=")
    val token = """formData\.append\('token',\s*'([^']+)'\)""".toRegex()
        .find(resumeBotDoc.select("script:containsData(download)").html())?.groupValues?.get(1)
    val sessionID = resumeBotResponse.header("Set-Cookie")
        ?.let { """PHPSESSID=([^;]+)""".toRegex().find(it)?.groupValues?.get(1) }

    val formData = mapOf(
        "token" to token,
    ).toString().toRequestBody()

    val client = OkHttpClient()
    val postRequest =
        Request.Builder().url(workerSeedUrl).header("Cookie", "PHPSESSID=" + sessionID!!).post(body = formData).build()
//    var postResponse = Jsoup.connect(workerSeedUrl).method(Connection.Method.POST)
//        .cookie("PHPSESSID", sessionID!!).data(formData).execute()
    val postResponse = client.newCall(postRequest).execute()

    val ddlUrl = """"url":"(.*?)"""".toRegex().find(postResponse.body.toString())?.groupValues?.get(1)
        ?.replace(oldValue = "\\", newValue = "")
//    println("Extracted URL: $ddlUrl")

    val title = resumeBotDoc.select("div.card-header").text()
//        val size: Long = resumeBotDoc.select("div.card-body li i.size").text().toLong()
//        println(title)
//        DDLStream(null.toString(), null.toString())
    return DDLStream(title, ddlUrl!!)
}
