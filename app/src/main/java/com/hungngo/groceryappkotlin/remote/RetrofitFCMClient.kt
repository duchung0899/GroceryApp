package com.hungngo.groceryappkotlin.remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFCMClient {
    fun getInstance(): Retrofit{
        val retrofit = Retrofit.Builder()
        retrofit.apply {
            baseUrl("https://fcm.googleapis.com/")
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
        }
        return retrofit.build()
    }
}