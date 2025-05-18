package com.hitsuthar.junescrapper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class JuneScrapperApplication
fun main(args: Array<String>) {
	runApplication<JuneScrapperApplication>(*args)
}
