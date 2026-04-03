package edu.nd.pmcburne.hello

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface CampusApiService {

    @GET("placemarks.json")
    suspend fun getPlacemarks(): List<PlacemarkDto>
}

object CampusApi {
    private const val BASE_URL = "https://www.cs.virginia.edu/~wxt4gm/"

    val retrofitService: CampusApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CampusApiService::class.java)
    }
}