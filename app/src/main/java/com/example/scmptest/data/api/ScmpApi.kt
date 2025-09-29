package com.example.scmptest.data.api

import com.example.scmptest.data.model.LoginRequest
import com.example.scmptest.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ScmpApi {
    @POST("login")
    suspend fun login(
        @Body request: LoginRequest,
        @Query("delay") delay: Int = 5
    ): Response<LoginResponse>
}