package com.akaliacius.demos.vertx_timer_service.utils;

import io.vertx.core.eventbus.DeliveryOptions;

import java.util.Optional;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.ACTION_KEY;

public class EventBusUtils {
  private static final long DEFAULT_TIMEOUT = 10_000;

  private EventBusUtils() {
    throw new RuntimeException("No instance of this class allowed");
  }

  public static DeliveryOptions setActionAndTimeout(String action, Optional<Long> timeout){
    return new DeliveryOptions()
      .addHeader(ACTION_KEY, action)
      .setSendTimeout(timeout.orElse(DEFAULT_TIMEOUT));
  }
}
