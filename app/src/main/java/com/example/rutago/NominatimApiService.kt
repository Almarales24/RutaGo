package com.example.rutago

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApiService {
    @Headers("User-Agent: RutaGo-App-Educativa")
    @GET("search")
    suspend fun buscarDireccion(
        @Query("q") texto: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5
    ): List<NominatimResult>
}