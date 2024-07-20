package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.Json;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class MainVerticle extends AbstractVerticle {

  private static final String BASE_URL = "https://jsonplaceholder.typicode.com/";
  private JsonPlaceholderApi jsonPlaceholderApi;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    Gson gson = new GsonBuilder()
      .setLenient()
      .create();

    Retrofit retrofit = new Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build();

    jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi.class);

    Router router = Router.router(vertx);
    router.get("/todos").handler(this::handleGetTodos);
    router.get("/todos/:id").handler(this::handleGetTodoById);

    vertx.createHttpServer().requestHandler(router).listen(8889).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8889");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }

  private void handleGetTodos(RoutingContext routingContext) {
    Call<List<Todo>> call = jsonPlaceholderApi.getTodos();
    call.enqueue(new Callback<List<Todo>>() {
      @Override
      public void onResponse(Call<List<Todo>> call, Response<List<Todo>> response) {
        if (response.isSuccessful()) {
          List<Todo> todos = response.body();
          if (todos != null) {
            String json = Json.encode(todos);
            routingContext.response()
              .putHeader("content-type", "application/json")
              .end(json);
          } else {
            routingContext.response().setStatusCode(500).end("No todos found");
          }
        } else {
          try {
            // Log the raw error body
            String errorBody = response.errorBody().string();
            System.err.println("Error body: " + errorBody);
            routingContext.response().setStatusCode(response.code()).end("Failed to fetch todos: " + errorBody);
          } catch (Exception e) {
            routingContext.response().setStatusCode(500).end("Failed to fetch todos");
          }
        }
      }

      @Override
      public void onFailure(Call<List<Todo>> call, Throwable t) {
        routingContext.response().setStatusCode(500).end("Error: " + t.getMessage());
      }
    });
  }

  private void handleGetTodoById(RoutingContext routingContext) {
    String id = routingContext.pathParam("id");
    Call<Todo> call = jsonPlaceholderApi.getTodoById(id);
    call.enqueue(new Callback<Todo>() {
      @Override
      public void onResponse(Call<Todo> call, Response<Todo> response) {
        if (response.isSuccessful()) {
          Todo todo = response.body();
          if (todo != null) {
            String json = Json.encode(todo);
            routingContext.response()
              .putHeader("content-type", "application/json")
              .end(json);
          } else {
            routingContext.response().setStatusCode(500).end("Todo not found");
          }
        } else {
          try {
            // Log the raw error body
            String errorBody = response.errorBody().string();
            System.err.println("Error body: " + errorBody);
            routingContext.response().setStatusCode(response.code()).end("Failed to fetch todo: " + errorBody);
          } catch (Exception e) {
            routingContext.response().setStatusCode(500).end("Failed to fetch todo");
          }
        }
      }

      @Override
      public void onFailure(Call<Todo> call, Throwable t) {
        routingContext.response().setStatusCode(500).end("Error: " + t.getMessage());
      }
    });
  }
}
