package com.example.todo.network

import com.example.todo.network.models.ServerResponse
import com.example.todo.network.models.TodoItemListRequest
import com.example.todo.network.models.TodoItemRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


private const val BASE_URL = "https://beta.mrdekk.ru/todobackend/"


private fun createInterceptor(): HttpLoggingInterceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level = HttpLoggingInterceptor.Level.BODY
    return interceptor
}

private val client = OkHttpClient.Builder()
    .addInterceptor(createInterceptor())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .baseUrl(BASE_URL)
    .build()


interface TodoApiService {
    @Headers("Authorization: Bearer unformulable")
    @GET("list")
    suspend fun getServerResponse(): ServerResponse

    @Headers("Authorization: Bearer unformulable")
    @POST("list")
    suspend fun addItem(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body itemRequest: TodoItemRequest
    )

    @Headers("Authorization: Bearer unformulable")
    @PUT("list/{id}")
    suspend fun updateItem(
        @Path("id") id: String,
        @Header("X-Last-Known-Revision") revision: Int,
        @Body itemRequest: TodoItemRequest
    )

    @Headers("Authorization: Bearer unformulable")
    @DELETE("list/{id}")
    suspend fun deleteItem(
        @Path("id") id: String,
        @Header("X-Last-Known-Revision") revision: Int
    )

    @Headers("Authorization: Bearer unformulable")
    @PATCH("list")
    suspend fun patchItemList(
        @Header("X-Last-Known-Revision") revision: Int,
        @Body itemRequest: TodoItemListRequest
    )
}

@Singleton
class TodoApi @Inject constructor() {
    val retrofitService: TodoApiService by lazy {
        retrofit.create(TodoApiService::class.java)
    }
}

