package com.hitsuthar.junescrapper.service

import com.hitsuthar.junescrapper.providers.*
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

data class MovieLinksResponse(
    val ddlLinks: List<DDLStream> // Wraps the list under the key "links"
)

data class DDLStream(
    val title: String, // e.g., "480p", "720p", "1080p"
    val url: String      // e.g., "http://example.com/download1"
)

@Service
class WebScraperService {
    val fetcher = DocumentFetcher()

    @Cacheable("movieDrive")
    fun getMoviesDriveService(title: String, year: Int?): MovieLinksResponse {
        return getMoviesDriveDDL(title, year, fetcher)
    }

    @Cacheable("OD")
    fun getOpenDirectoryService(query: String): MovieLinksResponse {
        return getOpenDirectory(query, fetcher)
    }

    @Cacheable("UHD")
    fun getUHDMoviesService(title: String, year: Int?): MovieLinksResponse {
        return getUHDMovies(title, year, fetcher)
    }

    @Cacheable("MoviesMod")
    fun getMoviesModService(title: String, year: Int?): MovieLinksResponse {
        return getMoviesMod(title, year, fetcher)
    }

    @Cacheable("RogMovies")
    fun getRogMoviesService(title: String, year: Int?): MovieLinksResponse {
        return getRogMovies(title, year, fetcher)
    }

}