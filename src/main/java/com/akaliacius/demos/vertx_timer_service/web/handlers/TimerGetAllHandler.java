package com.akaliacius.demos.vertx_timer_service.web.handlers;

import com.akaliacius.demos.vertx_timer_service.eventbus.EventBusMap;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.ACTION_LIST;
import static com.akaliacius.demos.vertx_timer_service.utils.EventBusUtils.setActionAndTimeout;
import static java.util.Optional.empty;

public class TimerGetAllHandler implements Handler<RoutingContext> {

  private static final Logger logger = LoggerFactory.getLogger(TimerGetAllHandler.class);

  @Override
  public void handle(RoutingContext context) {
    var eventBus = context.vertx().eventBus();
    eventBus.<JsonArray>request(EventBusMap.TIMER_MANAGER, new JsonObject(), setActionAndTimeout(ACTION_LIST, empty()), reply -> {
      if(reply.succeeded()){
        var jsonReply = reply.result().body();
        logger.info("timer manager replied: {}", jsonReply.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .setStatusCode(HttpResponseStatus.CREATED.code())
          .end(jsonReply.toBuffer());
      } else if(reply.failed()){
        context.response()
          .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
          .end(new JsonObject().put("error", reply.cause().getMessage()).toBuffer());
      }
    });
  }
}
