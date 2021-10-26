package com.akaliacius.demos.vertx_timer_service.web.handlers;

import com.akaliacius.demos.vertx_timer_service.eventbus.EventBusMap;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.ACTION_DELETE;
import static com.akaliacius.demos.vertx_timer_service.utils.EventBusUtils.setActionAndTimeout;
import static java.util.Optional.empty;

public class TimerDeleteHandler implements Handler<RoutingContext> {

  private static final Logger logger = LoggerFactory.getLogger(TimerDeleteHandler.class);

  @Override
  public void handle(RoutingContext context) {
    var json = context.getBodyAsJson();
    logger.debug("Request: {}", json.encode());
    var eventBus = context.vertx().eventBus();
    eventBus.<JsonObject>request(EventBusMap.TIMER_MANAGER, json, setActionAndTimeout(ACTION_DELETE, empty()), reply -> {
      if(reply.succeeded()){
        var jsonReply = reply.result().body();
        logger.info("timer manager replied: {}", jsonReply.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(HttpResponseStatus.ACCEPTED.code())
          .end(json.toBuffer());
      } else if(reply.failed()){
        context.response()
          .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
          .end(new JsonObject().put("error", reply.cause().getMessage()).toBuffer());
      }
    });
  }
}
