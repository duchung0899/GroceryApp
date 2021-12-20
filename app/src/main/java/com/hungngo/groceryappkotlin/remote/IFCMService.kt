package com.hungngo.groceryappkotlin.remote

import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.model.remoteFCM.FCMRespond
import com.hungngo.groceryappkotlin.model.remoteFCM.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=${MyCommon.FCM_KEY}"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMRespond>
}