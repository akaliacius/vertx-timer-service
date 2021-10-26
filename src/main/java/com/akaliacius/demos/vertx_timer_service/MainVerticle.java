package com.akaliacius.demos.vertx_timer_service;

import com.akaliacius.demos.vertx_timer_service.verticles.TimerManager;
import com.akaliacius.demos.vertx_timer_service.verticles.TimersRestApi;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.akaliacius.demos.vertx_timer_service.utils.LoggingUtils.logDeploymentFailure;
import static com.akaliacius.demos.vertx_timer_service.utils.LoggingUtils.logDeploymentSuccess;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.exceptionHandler(error -> logger.error("Unhandled: {}", error));
    vertx.deployVerticle(new MainVerticle())
      .onFailure(error -> logDeploymentFailure(error, MainVerticle.class, logger))
      .onSuccess(id -> logDeploymentSuccess(id, MainVerticle.class, logger));
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(new TimerManager())
      .onSuccess(id -> logDeploymentSuccess(id, TimerManager.class, logger))
      .onFailure(error -> {
        logDeploymentFailure(error, TimerManager.class, logger);
        startPromise.fail(error);
      })
      .compose(next -> deployRestApiVerticle(startPromise));
  }

  private Future<String> deployRestApiVerticle(Promise<Void> startPromise){
    return vertx.deployVerticle(
        TimersRestApi.class.getName(),
        new DeploymentOptions().setInstances(2)
      )
      .onFailure(error -> {
        logDeploymentFailure(error, TimersRestApi.class, logger);
        startPromise.fail(error);
      })
      .onSuccess(id -> {
        logDeploymentSuccess(id, TimersRestApi.class, logger);
        startPromise.complete();
      });
  }
}
