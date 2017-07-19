package com.example.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import utils.WireMockExtension
import java.util.*

@ExtendWith(WireMockExtension::class)
internal class MoviesServiceGatewayTest {

    val config = MoviesServiceConfig()
    val cut = MoviesServiceGateway(config)

    @BeforeEach fun configureGateway(server: WireMockServer) {
        config.port = server.port()
    }

    @Nested inner class `get movie by ID` {

        @Test fun `existing movie can be got`() {
            val id = UUID.randomUUID()
            stubFor(get(urlEqualTo("/movies/$id"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .withBody(asJson("{ 'title': 'Iron Man', 'imdbScore': 7.9 }"))))

            val movie = cut.getMovie(id)!!
            
            assertThat(movie.title).isEqualTo("Iron Man")
            assertThat(movie.imdbScore).isEqualTo(7.9f, offset(0.01f))
        }

        @Test fun `non-existing movie is returned as null`() {
            val id = UUID.randomUUID()
            stubFor(get(urlEqualTo("/movies/$id"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse().withStatus(404)))

            assertThat(cut.getMovie(id)).isNull()
        }

    }

    @Nested inner class `get movies` {

        @Test fun `existing movies can be got`() {
            val batmanBegins = "{ 'title': 'Batman Begins', 'imdbScore': 8.3 }"
            val theDarkKnight = "{ 'title': 'The Dark Knight', 'imdbScore': 9.0 }"
            val theDarkKnightRises = "{ 'title': 'The Dark Knight Rises', 'imdbScore': 8.5 }"

            stubFor(get(urlEqualTo("/movies"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .withBody(asJson("{ 'movies': [ $batmanBegins, $theDarkKnight, $theDarkKnightRises ] }"))))

            val movies = cut.getMovies()

            assertThat(movies).hasSize(3)
            assertThat(movies[0].title).isEqualTo("Batman Begins")
            assertThat(movies[0].imdbScore).isEqualTo(8.3f, offset(0.01f))
            assertThat(movies[1].title).isEqualTo("The Dark Knight")
            assertThat(movies[1].imdbScore).isEqualTo(9.0f, offset(0.01f))
            assertThat(movies[2].title).isEqualTo("The Dark Knight Rises")
            assertThat(movies[2].imdbScore).isEqualTo(8.5f, offset(0.01f))
        }

        @Test fun `if there are no movies an empty list is returned (empty JSON array)`() {
            stubFor(get(urlEqualTo("/movies"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .withBody(asJson("{ 'movies': [] }"))))

            assertThat(cut.getMovies()).isEmpty()
        }

        @Test fun `if there are no movies an empty list is returned (no field)`() {
            stubFor(get(urlEqualTo("/movies"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .withBody(asJson("{}"))))

            assertThat(cut.getMovies()).isEmpty()
        }

    }

    private fun asJson(value: String) = value.replace("'", "\"")

}