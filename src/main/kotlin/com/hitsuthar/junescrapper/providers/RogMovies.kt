package com.hitsuthar.junescrapper.providers

import com.hitsuthar.junescrapper.extractors.Extractor
import com.hitsuthar.junescrapper.service.DDLStream
import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import com.hitsuthar.junescrapper.utils.rogMoviesUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

fun rogMoviesSearch(
	query: String, year: Int?, type: String?, season: Int?, fetcher: DocumentFetcher
): List<String> {
	println(query)
	val searchUrl = if (type == "movie") "$rogMoviesUrl/?s=${formattedQuery("$query $year")}" else "$rogMoviesUrl/?s=${
		formattedQuery(query)
	}"
	val searchResults: MutableList<String> = emptyList<String>().toMutableList()
	return try {
		Jsoup.parse(fetcher.fetchWithRetries(searchUrl)).select("div.blog-pic a.blog-img").filter { element: Element ->
			element.attr("title").contains(query, true) && element.attr("title").contains("season $season", true)
		}.map {
			searchResults.add(it.attr("href"))
		}
		if (searchResults.isEmpty()) emptyList() else searchResults
	} catch (e: Exception) {
		emptyList()
	}
}

fun getRogMovies(
	title: String, year: Int?, type: String?, season: Int?, episode: Int?, fetcher: DocumentFetcher
): MovieLinksResponse {
	try {
		val searchResultUrl = rogMoviesSearch(title, year, type, season, fetcher)
		if (searchResultUrl.isEmpty()) {
			return MovieLinksResponse(ddlLinks = emptyList()) // Return an empty list if no results
		}
		val ddlStreams: MutableList<DDLStream> = emptyList<DDLStream>().toMutableList()

		searchResultUrl.map { resultURL ->
			if (type == "movie") {
				Jsoup.parse(fetcher.fetchWithRetries(resultURL)).select("div.entry-content > p > a").parallelStream()
					.map { element ->
						val link = element.attr("href")
						val nextDrive = Jsoup.parse(fetcher.fetchWithRetries(link)).select("p a").firstOrNull { nextDriveElement ->
							nextDriveElement.select("button").any{ button ->
								button.text().contains("V-Cloud", ignoreCase = true)
							}
						}?.attr("href")
						if (nextDrive != null) {
							val vcloud = Jsoup.parse(fetcher.fetchWithRetries(nextDrive)).select("script:containsData(url)").html()
							val hubCloud = Regex("var url = '([^']*)'").find(vcloud)?.groupValues?.get(1)
							if (hubCloud != null) {
								val ddl = Extractor().extractFromHUbCloud(hubCloud, fetcher)
								if (ddl != null) {
									ddlStreams.add(ddl)
									println(ddl)
								}
							}
						}
					}.toList()
			} else if (type == "tv") {
				Jsoup.parse(fetcher.fetchWithRetries(resultURL)).select("div.entry-content > p a").filter { a ->
					a.text().contains("V-Cloud", ignoreCase = true)
				}.map { a: Element ->
					val nextDriveURL = a.attr("href")
//					println(nextDriveURL)
					val episodeH4 = Jsoup.parse(fetcher.fetchWithRetries(nextDriveURL)).select("h4").firstOrNull { h4 ->
						h4.text().contains("Ep", ignoreCase = true)
						h4.text().contains(episode.toString())
					}
					if (episodeH4 != null) {
						// The <p> we want is the next sibling element after the h4
						val downloadP = episodeH4.nextElementSibling()

						if (downloadP != null && downloadP.tagName() == "p") {
							// Extract all href links from the <p> element
							println(
								downloadP.select("a").firstOrNull { episodeA ->
									episodeA.attr("href").contains("vcloud", ignoreCase = true) && !a.attr("href").contains("api")
								}
									?.attr("href")
							)
						}
					}

				}
			} else {
				return MovieLinksResponse(ddlLinks = emptyList())
			}
		}


		return MovieLinksResponse(ddlStreams)
	} catch (e: Exception) {
		println(e)
		return MovieLinksResponse(ddlLinks = emptyList())
	}
}