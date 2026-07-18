package com.kafkaddd.chat.web;

import java.util.List;

/** Uniform error body across every controller. Never carries a raw internal exception message. */
public record ApiError(String error, List<String> details) {

  public static ApiError of(String error) {
    return new ApiError(error, List.of());
  }

  public static ApiError of(String error, List<String> details) {
    return new ApiError(error, details);
  }
}
