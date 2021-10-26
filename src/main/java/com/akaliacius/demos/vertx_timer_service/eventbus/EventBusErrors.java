package com.akaliacius.demos.vertx_timer_service.eventbus;

public enum EventBusErrors {
    CREATE_TIMER_ERROR(5, "error creating timer"),
    DUPLICATE_ENDPOINT_ERROR(2, "existing endpoint error"),
    NON_EXISTING_ENDPOINT_ERROR(6, "non existing endpoint error");

  private final int errorCode;
  private final String errorMessage;

  public int errorCode(){
    return errorCode;
  }

  public String errorMessage(){
    return errorMessage;
  }

  EventBusErrors(int errorCode, String errorMessage) {
    this.errorMessage = errorMessage;
    this.errorCode = errorCode;
  }
}
