package com.example.client

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

class MoviesServiceGateway(
        private val config: MoviesServiceConfig,
        private val restTemplate: RestTemplate = RestTemplate()
) {

    fun getMovie(id: UUID): Movie? {
        try {
            val url = "http://${config.host}:${config.port}/movies/$id"
            val response = restTemplate.getForEntity(url, Movie::class.java)
            return response.body
        } catch (e: HttpClientErrorException) {
            if (e.statusCode == HttpStatus.NOT_FOUND) return null else throw e
        }
    }

    fun getMovies(): List<Movie> {
        val url = "http://${config.host}:${config.port}/movies"
        val response = restTemplate.getForEntity(url, MovieList::class.java)
        return response.body.movies
    }

}