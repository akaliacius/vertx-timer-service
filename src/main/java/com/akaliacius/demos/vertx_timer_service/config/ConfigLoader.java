package com.akaliacius.demos.vertx_timer_service.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static com.akaliacius.demos.vertx_timer_service.utils.Constants.SERVER_PORT;

public class ConfigLoader {
  private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
  static final List<String> EXPOSED_ENV_VAR = List.of(
    SERVER_PORT
  );
  public static Future<JsonObject> load(Vertx vertx){
    final var exposedKeys = new JsonArray(EXPOSED_ENV_VAR);
    logger.debug("Fetch configuration for {}", exposedKeys.encode());
    var envStore = new ConfigStoreOptions()
      .setType("env")
      .setConfig(new JsonObject().put("keys", exposedKeys));

    var retriever = ConfigRetriever.create(
      vertx,
      new ConfigRetrieverOptions()
        .addStore(envStore)
    );
    return retriever.getConfig()
      .map(ConfigLoader::validate);
  }

  private static JsonObject validate(JsonObject props){
    logger.info("props {}", props.encode());
    Objects.requireNonNull(props.getInteger(SERVER_PORT), SERVER_PORT + " is not set");
    return props;
  }

}
