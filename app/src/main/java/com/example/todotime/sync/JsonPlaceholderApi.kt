package com.example.todotime.sync

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class JsonPlaceholderTodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

data class JsonPlaceholderCreateTodoRequest(
    val userId: Int,
    val title: String,
    val completed: Boolean
)

interface JsonPlaceholderApi {
    @GET("todos")
    suspend fun getTodos(
        @Query("_limit") limit: Int = 30
    ): List<JsonPlaceholderTodoDto>

    @POST("todos")
    suspend fun createTodo(
        @Body request: JsonPlaceholderCreateTodoRequest
    ): JsonPlaceholderTodoDto
}
