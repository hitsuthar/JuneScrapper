package com.hitsuthar.junescrapper.service

import com.hitsuthar.junescrapper.providers.*
import com.hitsuthar.junescrapper.utils.DocumentFetcher
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

data class MovieLinksResponse(
    val ddlLinks: List<DDLStream> // Wraps the list under the key "links"
)

data class DDLStream(
    val name: String, // e.g., "480p", "720p", "1080p"
    val url: String,      // e.g., "http://example.com/download1"
    val size: String? = null
)

@Service
class WebScraperService {
    val fetcher = DocumentFetcher()

    @Cacheable("movieDrive")
    fun getMoviesDriveService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getMoviesDriveDDL(title, year, type, season, episode, fetcher)
    }

    @Cacheable("OD")
    fun getOpenDirectoryService(query: String): MovieLinksResponse {
        return getOpenDirectory(query, fetcher)
    }

    @Cacheable("UHD")
    fun getUHDMoviesService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getUHDMovies(title, year, type, season, episode, fetcher)
    }

    @Cacheable("MoviesMod")
    fun getMoviesModService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getMoviesMod(title, year, type, season, episode, fetcher)
    }

    @Cacheable("RogMovies")
    fun getRogMoviesService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getRogMovies(title, year, type, season, episode, fetcher)
    }

    @Cacheable("Full4Movies")
    fun getFull4MoviesService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getFull4Movies(title, year, type, season, episode, fetcher)
    }

    @Cacheable("VegaMovies")
    fun getVegaMoviesService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getVegaMovies(title, year, type, season, episode, fetcher)
    }

//    @Cacheable("KatMovieHD")
//    fun getKatMovieHDService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
//        return getKatMoviesHD(title, year, type, season, episode, fetcher)
//    }

    @Cacheable("LuxeCinema")
    fun getLuxeCinemaService(title: String, year: Int?, type: String?, season: Int?, episode: Int?): MovieLinksResponse {
        return getLuxeCinema(title, year, type, season, episode, fetcher)
    }
}