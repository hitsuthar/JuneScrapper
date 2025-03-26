package com.hitsuthar.junescrapper.controller

import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.service.WebScraperService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class MovieController(private val webScraperService: WebScraperService) {
    @GetMapping("/moviesdrive")
    fun getMovieDriveDDL(
        @RequestParam name: String?,
        @RequestParam(required = false) year: Int?
    ): MovieLinksResponse {
        val links = name?.let { webScraperService.getMoviesDriveService(it, year) }
        if (links != null) return links
        return MovieLinksResponse(emptyList())
    }

    @GetMapping("/od")
    fun getOpenDirectoryDDL(
        @RequestParam name: String?,
    ): MovieLinksResponse {
        val links = name?.let { webScraperService.getOpenDirectoryService(name) }
        if (links != null) return links
        return MovieLinksResponse(emptyList())
    }

    @GetMapping("/uhd")
    fun getUHDMoviesDDL(
        @RequestParam name: String?,
        @RequestParam(required = false) year: Int?
    ): MovieLinksResponse {
        val links = name?.let { webScraperService.getUHDMoviesService(it, year) }
        if (links != null) return links
        return MovieLinksResponse(emptyList())
    }

    @GetMapping("/moviesmod")
    fun getMoviesModDL(
        @RequestParam name: String?,
        @RequestParam(required = false) year: Int?
    ): MovieLinksResponse {
        val links = name?.let { webScraperService.getMoviesModService(it, year) }
        return MovieLinksResponse(emptyList())
    }
}