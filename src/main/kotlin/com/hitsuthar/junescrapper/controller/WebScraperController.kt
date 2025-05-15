package com.hitsuthar.junescrapper.controller

import com.hitsuthar.junescrapper.service.MovieLinksResponse
import com.hitsuthar.junescrapper.service.WebScraperService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


data class ApiDescription(
	val name: String? = null,
	val path: String? = null,
)

@RestController
class DiscoveryController {
	@GetMapping("/discover")
	fun discoverApis(): List<ApiDescription> {
		val apiList: MutableList<ApiDescription> = ArrayList()

		// Manually register APIs (or automate via reflection)
		apiList.add(
			ApiDescription(
				"Movies Drive",
				"/api/moviesdrive",
			)
		)

		apiList.add(
			ApiDescription(
				"Open Directory",
				"/api/od",
			)
		)

		apiList.add(
			ApiDescription(
				"UHD Movies",
				"/api/uhd",
			)
		)

		apiList.add(
			ApiDescription(
				"MoviesMod",
				"/api/moviesmod",
			)
		)

		return apiList
	}
}

@RestController
@RequestMapping("/api")
class MovieController(private val webScraperService: WebScraperService) {

	@GetMapping("/moviesdrive")
	fun getMovieDriveDDL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getMoviesDriveService(title, year, type, season, episode)
		}
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
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getUHDMoviesService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

	@GetMapping("/moviesmod")
	fun getMoviesModDLL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getMoviesModService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

	@GetMapping("/rogmovies")
	fun getRogMoviesDDL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getRogMoviesService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

	@GetMapping("/full4movies")
	fun getFull4MoviesDDL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getFull4MoviesService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

	@GetMapping("/vegamovies")
	fun getVegaMoviesDDL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getVegaMoviesService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

	@GetMapping("/luxecinema")
	fun getLuxeCinemaDDL(
		@RequestParam title: String?,
		@RequestParam(required = false) year: Int?,
		@RequestParam type: String?,
		@RequestParam(required = false) season: Int?,
		@RequestParam(required = false) episode: Int?,
	): MovieLinksResponse {
		if (title == null || type == null) {
			if (season == null || episode == null) return MovieLinksResponse(emptyList())
		} else {
			return webScraperService.getLuxeCinemaService(title, year, type, season, episode)
		}
		return MovieLinksResponse(emptyList())
	}

//    @GetMapping("/katmoviehd")
//    fun getKatMoviesDDL(
//        @RequestParam name: String?,
//        @RequestParam(required = false) year: Int?
//    ): MovieLinksResponse {
//        val links = name?.let { webScraperService.getKatMovieHDService(it, year) }
//        if (links != null) return links
//        return MovieLinksResponse(emptyList())
//    }


}