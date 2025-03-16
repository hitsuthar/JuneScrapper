package com.hitsuthar.junescrapper.extractors

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import org.jsoup.Jsoup

fun getHubCloudUrl(
    url: String,
): DDLStream {
    return try {
        var ddlLink: String = null.toString()
        val doc = Jsoup.connect(url).get()

        val header = doc.getElementsByTag("title").text()
        val size = doc.select("i[id=\"size\"]").first()?.text()?.replace(oldValue = " ", newValue = "")

        val link = if (url.contains("drive")) {
            val scriptTag = doc.selectFirst("script:containsData(url)")?.toString() ?: ""
            Regex("var url = '([^']*)'").find(scriptTag)?.groupValues?.get(1) ?: ""
        } else doc.selectFirst("div.vd > center > a")?.attr("href") ?: ""

        val document = Jsoup.connect(link).get()
        val div = document.selectFirst("div.card-body")
        div?.select("h2 a.btn")?.mapNotNull { element ->
            val link1 = element.attr("href")
            val text = element.text()
            when {
                text.contains("Download [FSL Server]") -> ddlLink = link1
                text.contains("Download File") -> ddlLink = link1
                text.contains("PixelServer") -> ddlLink = link1
                else -> null
            }
        }
        DDLStream("${getNameAndQuality(header)} $size", ddlLink)
    } catch (e: Exception) {
        DDLStream(null.toString(), null.toString())
    }
}
