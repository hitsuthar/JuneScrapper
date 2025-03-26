package com.hitsuthar.junescrapper.extractors

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.fetchDocumentWithRetries
import com.hitsuthar.junescrapper.utils.formatBytes
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.boot.json.GsonJsonParser
import org.springframework.boot.json.JsonParser

fun getDriveSeed(url: String): DDLStream {
//        println("hehe")
    val DL = Jsoup.connect(url).get()
    if (DL.select("a.btn-warning").text().contains(" Resume Cloud ")) {
        println(DL.select("a.btn-warning").attr("href"))
    }
    val resumeBotUrl = DL.select("a.btn-light").attr("href")
//        val ddl = DL.select("a.btn").attr("href")
    println("resumeurl $resumeBotUrl")
    val resumeBotResponse = Jsoup.connect(resumeBotUrl).execute()

    val workerSeedUrl = resumeBotUrl.substringBefore("&do=")
    val token = """formData\.append\('token',\s*'([^']+)'\)""".toRegex()
        .find(resumeBotResponse.parse().select("script:containsData(download)").html())?.groupValues?.get(1)
    val sessionID = resumeBotResponse.header("Set-Cookie")
        ?.let { """PHPSESSID=([^;]+)""".toRegex().find(it)?.groupValues?.get(1) }.toString()

    val formData = mapOf(
        "token" to token,
    )

    val postResponse = Jsoup.connect(workerSeedUrl).method(Connection.Method.POST)
        .cookie("PHPSESSID", sessionID).data(formData).execute()

    val ddlUrl = """"url":"(.*?)"""".toRegex().find(postResponse.body().toString())?.groupValues?.get(1).toString().replace(oldValue = "\\", newValue = "")
    println("Extracted URL: $ddlUrl")

//        val title = resumeBotResponse.select("div.card-header").text()
//        val size: Long = resumeBotResponse.select("div.card-body li i.size").text() as Long
//        println(title)
//        DDLStream(null.toString(), null.toString())
    return DDLStream(null.toString(), null.toString())
}
