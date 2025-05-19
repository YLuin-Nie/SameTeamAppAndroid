package com.example.sameteamappandroid

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
   // private const val BASE_URL = "http://10.0.2.2:5073/api/"
    private const val BASE_URL = "https://sameteamapiazure-gfawexgsaph0cvg2.centralus-01.azurewebsites.net/api/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
