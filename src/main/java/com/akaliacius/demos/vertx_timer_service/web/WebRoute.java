package com.akaliacius.demos.vertx_timer_service.web;

import com.akaliacius.demos.vertx_timer_service.web.handlers.TimerCreateHandler;
import com.akaliacius.demos.vertx_timer_service.web.handlers.TimerDeleteHandler;
import com.akaliacius.demos.vertx_timer_service.web.handlers.TimerGetAllHandler;
import com.akaliacius.demos.vertx_timer_service.web.handlers.TimerUpdateHandler;
import io.vertx.ext.web.Router;
import java.util.function.BiConsumer;

public enum WebRoute {

  ADD_TIMER_POST("/timers", (router, p) -> router.post(p).handler(new TimerCreateHandler())),
  UPDATE_TIMER_PUT("/timers", (router, p) -> router.put(p).handler(new TimerUpdateHandler())),
  CANCEL_TIMER_DELETE("/timers", (router, p) -> router.delete(p).handler(new TimerDeleteHandler())),
  LIST_TIMERS_GET("/timers", (router, p) -> router.get(p).handler(new TimerGetAllHandler()));

  private final String path;
  private final BiConsumer<Router, String> routerConsumer;

  WebRoute(String path, BiConsumer<Router, String> routerConsumer){
    this.path = path;
    this.routerConsumer = routerConsumer;
  }

  public String getPath() {
    return path;
  }

  public void attach(Router router, String path){
    routerConsumer.accept(router, path);
  }
}
