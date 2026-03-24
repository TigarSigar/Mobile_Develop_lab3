package com.example.todotime.sync

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object JsonPlaceholderClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: JsonPlaceholderApi by lazy {
        retrofit.create(JsonPlaceholderApi::class.java)
    }
}
