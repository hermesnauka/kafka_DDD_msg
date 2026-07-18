package com.kafkaddd.chat.web;

import com.kafkaddd.chat.chatdelivery.domain.NoSuchRoomException;
import com.kafkaddd.chat.chatdelivery.domain.NotAParticipantException;
import com.kafkaddd.chat.identity.application.EmailAlreadyInUseException;
import com.kafkaddd.chat.identity.application.InvalidCredentialsException;
import com.kafkaddd.chat.identity.application.InvalidRefreshTokenException;
import com.kafkaddd.chat.identity.application.NoSuchUserException;
import com.kafkaddd.chat.identity.domain.AccountLockedException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain/application exceptions into {@link ApiError} responses.
 * Every case here is one this project's own code throws deliberately, with
 * a message already meant to be user-facing — except the
 * {@link IllegalArgumentException} handler, which is a broader net (it also
 * catches domain VO validation like {@code Email}/{@code DisplayName}) and
 * assumes every {@code IllegalArgumentException} reachable from a
 * controller in this codebase carries a safe message; a future
 * general-purpose library call that also throws
 * {@code IllegalArgumentException} could leak an unintended message through
 * this same path.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyInUseException.class)
  ResponseEntity<ApiError> handle(EmailAlreadyInUseException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ApiError> handle(InvalidCredentialsException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(AccountLockedException.class)
  ResponseEntity<ApiError> handle(AccountLockedException e) {
    return ResponseEntity.status(HttpStatus.LOCKED).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  ResponseEntity<ApiError> handle(InvalidRefreshTokenException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(NoSuchUserException.class)
  ResponseEntity<ApiError> handle(NoSuchUserException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(NoSuchRoomException.class)
  ResponseEntity<ApiError> handle(NoSuchRoomException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(NotAParticipantException.class)
  ResponseEntity<ApiError> handle(NotAParticipantException e) {
    // SR-5 requires blocking content access to non-participants, which this
    // does — but note 403 (not a participant) vs. 404 (NoSuchRoomException)
    // are still distinguishable, so a caller can tell "this room exists but
    // isn't mine" from "this room doesn't exist" (room-ID enumeration, not
    // a credential/account-level leak). Unlike InvalidCredentialsException's
    // deliberate collapsing of "unknown email" vs. "wrong password", this
    // hasn't been collapsed — flagged here as a conscious choice, not an
    // oversight, should that tradeoff need revisiting later.
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  ResponseEntity<ApiError> handle(MissingRequestCookieException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("missing required cookie: " + e.getCookieName()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ApiError> handle(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of(e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handle(MethodArgumentNotValidException e) {
    List<String> details =
        e.getBindingResult().getFieldErrors().stream().map(this::describe).toList();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError.of("validation failed", details));
  }

  private String describe(FieldError error) {
    return error.getField() + ": " + error.getDefaultMessage();
  }
}
