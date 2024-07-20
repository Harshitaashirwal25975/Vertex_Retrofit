package com.example.starter;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface JsonPlaceholderApi {
  @GET("/todos")
  Call<List<Todo>> getTodos();

  @GET("/todos/{id}")
  Call<Todo> getTodoById(@Path("id") String id);
}
