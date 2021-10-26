package com.akaliacius.demos.vertx_timer_service.utils;

import org.slf4j.Logger;

public final class LoggingUtils {

  private LoggingUtils() {
    throw new RuntimeException("No instance of this class allowed");
  }

  public static void logDeploymentSuccess(String id, Class klass, Logger logger){
    logger.info("Deployed {} with id:{}", klass.getSimpleName(), id);
  }

  public static void logDeploymentFailure(Throwable error, Class klass, Logger logger){
    logger.info("Deployment of {} failed", klass.getSimpleName(), error);
  }
}
