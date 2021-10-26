package com.akaliacius.demos.vertx_timer_service.verticles;

import com.akaliacius.demos.vertx_timer_service.eventbus.EventBusMap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.akaliacius.demos.vertx_timer_service.eventbus.EventBusErrors.*;
import static com.akaliacius.demos.vertx_timer_service.utils.Constants.*;
import static com.akaliacius.demos.vertx_timer_service.utils.LoggingUtils.logDeploymentFailure;
import static com.akaliacius.demos.vertx_timer_service.utils.LoggingUtils.logDeploymentSuccess;

public class TimerManager extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(TimerManager.class);
  private Map<String, Long> timers = new HashMap<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(
      TimerHttpClient.class.getName(),
      new DeploymentOptions().setInstances(4)
    )
      .onSuccess(id -> {
        logDeploymentSuccess(id, TimerHttpClient.class, logger);
        startEventBus();
        startPromise.complete();
      })
      .onFailure(error -> {
        logDeploymentFailure(error, TimerHttpClient.class, logger);
        startPromise.fail(error);
      });
  }

  private void startEventBus(){
    vertx.eventBus().<JsonObject>consumer(EventBusMap.TIMER_MANAGER, message -> {
      var action = message.headers().get(ACTION_KEY);
      switch (action) {
        case ACTION_CREATE:
          createTimer(message);
          break;
        case ACTION_UPDATE:
          deleteOrUpdateTimer(message, true);
          break;
        case ACTION_DELETE:
          deleteOrUpdateTimer(message, false);
          break;
        case ACTION_LIST:
          list(message);
          break;

      }
    });
  }

  private void list(Message<JsonObject> message){
    var array = new JsonArray();
    timers.keySet().forEach(array::add);
    message.reply(array);
  }

  private void createTimer(Message<JsonObject> message){
    var json = message.body();
    if(timers.containsKey(json.getString(ENDPOINT_KEY))){
      logger.error("duplicate endpoint {}", json.getString(ENDPOINT_KEY));
      message.fail(DUPLICATE_ENDPOINT_ERROR.errorCode(), DUPLICATE_ENDPOINT_ERROR.errorMessage());
      return;
    }
    startTimer(message);
  }

  private void deleteOrUpdateTimer(Message<JsonObject> message, boolean create){
    var json = message.body();
    var endpoint = json.getString(ENDPOINT_KEY);
    if(!timers.containsKey(endpoint)){
      logger.error("trying update non existing endpoint {}", endpoint);
      message.fail(NON_EXISTING_ENDPOINT_ERROR.errorCode(), NON_EXISTING_ENDPOINT_ERROR.errorMessage());
      return;
    }
    logger.info("deleting endpoint {} with id {}", endpoint, timers.get(endpoint));
    vertx.cancelTimer(timers.get(endpoint));
    timers.remove(endpoint);
    if(create){
      startTimer(message);
    } else {
      logger.info("endpoint {} deleted", endpoint);
      message.reply(new JsonObject().put("status", "deleted").put(ENDPOINT_KEY, endpoint));
    }
  }

  private void startTimer(Message<JsonObject> message){
    vertx.eventBus().<JsonObject>request(EventBusMap.START_TIMER, message.body(), reply -> {
      if(reply.succeeded()){
        var replyJson = reply.result().body();
        logger.info("received response from {}: {}", TimerHttpClient.class.getSimpleName(), replyJson.encode());
        timers.put(replyJson.getString(ENDPOINT_KEY), replyJson.getLong(ID_KEY));
        message.reply(replyJson);
      } else if(reply.failed()){
        logger.warn("failed response from {}", TimerHttpClient.class.getSimpleName());
        message.fail(CREATE_TIMER_ERROR.errorCode(), CREATE_TIMER_ERROR.errorMessage());
      }
    });
  }
}
