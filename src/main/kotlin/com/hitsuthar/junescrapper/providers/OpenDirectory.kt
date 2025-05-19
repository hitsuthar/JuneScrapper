package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.getNameAndQuality
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun getOpenDirectory(
	title: String,
	year: Int?,
	type: String?,
	season: Int?,
	episode: Int?,
	fetcher: DocumentFetcher
): MovieLinksResponse {
	val query =
		when (type) {
			"movie" -> "$title ${year ?: ""}"
			"tv" -> "$title ${if (season!! >= 10) "S$season" else "S0$season"}E${if (episode!! >= 10) episode else "0$episode"}"
			else -> ""
		}
	println(query)
	val url = "https://filepursuit.com/pursuit?q=$query&type=video&sort=sizedesc"
	val document: Document = Jsoup.parse(fetcher.fetchWithRetries(url))

	val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()
	document.select("div.file-post-item").forEach { row ->
		val fileName = row.select("h5").text()
		val size = row.select("div span.bg-primary").last()?.text()?.replace(oldValue = " ", newValue = "")

		val copyLink = row.select("a").attr("onclick")
		val linkRegex = Regex("'([^']+)'")
		val link = linkRegex.find(copyLink)?.groupValues?.get(1)

		ddlStreams.add(DDLStream("${getNameAndQuality(fileName)} $size", link!!))
	}
	return MovieLinksResponse(ddlLinks = ddlStreams)
}