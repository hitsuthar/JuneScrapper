package com.hitsuthar.junescrapper.service

import org.jsoup.Jsoup

class WebScraperService {
    fun scrapeDDLLinks(movieName: String): List<String> {
        val url = "https://example.com/search?q=$movieName" // Replace with the actual URL
        val document = Jsoup.connect(url).get()
        val links = mutableListOf<String>()

        // Example: Extract DDL links from the page
        document.select("a[href]").forEach { element ->
            val href = element.attr("href")
            if (href.contains("download")) { // Adjust the condition based on the website
                links.add(href)
            }
        }

        return links
    }
}