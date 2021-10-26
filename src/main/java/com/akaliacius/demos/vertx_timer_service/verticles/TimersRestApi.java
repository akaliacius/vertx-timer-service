package com.akaliacius.demos.vertx_timer_service.verticles;

import com.akaliacius.demos.vertx_timer_service.config.ConfigLoader;
import com.akaliacius.demos.vertx_timer_service.web.WebRoute;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.SERVER_PORT;

public class TimersRestApi extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(TimersRestApi.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    ConfigLoader.load(vertx)
      .onFailure(startPromise::fail)
      .onSuccess(config -> {
        logger.info("Retrieved Configuration: {}", config);
        startAndRouteServer(startPromise, config);
      });
  }

  private void startAndRouteServer(Promise<Void> startPromise, JsonObject config){
    var router = Router.router(vertx);
    router.route()
      .handler(BodyHandler.create())
      .failureHandler(handleFailure());

    Arrays.stream(WebRoute.values())
      .forEach(webRoute -> webRoute.attach(router, webRoute.getPath()));

    vertx.createHttpServer()
      .requestHandler(router)
      .exceptionHandler(error -> logger.error("HTTP Server error: {}", error))
      .listen(config.getInteger(SERVER_PORT), http -> {
        if (http.succeeded()) {
          startPromise.complete();
          logger.info("HTTP server started on port {}", SERVER_PORT);
        } else {
          startPromise.fail(http.cause());
        }
      });
  }

  private Handler<RoutingContext> handleFailure() {
    return errorContext -> {
      if(!errorContext.response().ended()){
        logger.error("Router Error:{}" + errorContext.failure());
        errorContext.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(500)
          .end(new JsonObject().put("message", errorContext.failure().getMessage()).toBuffer());
      }
    };
  }
}
