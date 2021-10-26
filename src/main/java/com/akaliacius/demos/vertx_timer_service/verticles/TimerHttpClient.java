package com.akaliacius.demos.vertx_timer_service.verticles;

import com.akaliacius.demos.vertx_timer_service.eventbus.EventBusMap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.*;

public class TimerHttpClient extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(TimerHttpClient.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    startPromise.complete();
    var eventBus = vertx.eventBus();
    eventBus.consumer(EventBusMap.START_TIMER, this::startTimer)
      .exceptionHandler(error -> logger.error("failed start timer", error));
  }

  private void startTimer(Message<JsonObject> message){
    var json = message.body();
    var delaySeconds = json.getLong(DELAY_KEY) * 1000;
    var endpoint = json.getString(ENDPOINT_KEY);
    var port = json.getInteger(PORT_KEY);
    vertx.setPeriodic(delaySeconds, id -> {
      sendTime(id, endpoint, port);
      message.reply(new JsonObject().put(ENDPOINT_KEY, endpoint).put("id", id));
    });
  }

  private void sendTime(long id, String endpoint, int port){
    var time = ZonedDateTime.now(ZoneId.systemDefault());
    var json = new JsonObject()
      .put(ID_KEY, id)
      .put(TIME_KEY, time.toString());
    var webclient = WebClient.create(vertx);
    webclient.post(endpoint)
      .port(port)
      .sendJsonObject(json)
      .onFailure(error -> logger.error("failed send request to {}", endpoint, error));
  }
}
