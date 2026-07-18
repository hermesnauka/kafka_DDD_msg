package com.kafkaddd.chat.identity.infrastructure;

import com.kafkaddd.chat.identity.application.AuthResult;
import com.kafkaddd.chat.identity.application.CurrentUserQuery;
import com.kafkaddd.chat.identity.application.LoginUseCase;
import com.kafkaddd.chat.identity.application.RefreshTokenUseCase;
import com.kafkaddd.chat.identity.application.RegisterUserUseCase;
import com.kafkaddd.chat.identity.application.UserView;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PLAN_SSDLC.md §7.1. {@code /register} does not auto-login — it only
 * creates the account; the client calls {@code /login} next. Tokens never
 * appear in a response body, only in {@code HttpOnly} cookies (SR-2).
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final CurrentUserQuery currentUserQuery;
  private final AuthCookies cookies;

  AuthController(
      RegisterUserUseCase registerUserUseCase,
      LoginUseCase loginUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      CurrentUserQuery currentUserQuery,
      AuthCookies cookies) {
    this.registerUserUseCase = registerUserUseCase;
    this.loginUseCase = loginUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.currentUserQuery = currentUserQuery;
    this.cookies = cookies;
  }

  @PostMapping("/register")
  ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    UserView view = registerUserUseCase.register(request.email(), request.password(), request.displayName());
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(view));
  }

  @PostMapping("/login")
  ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthResult result = loginUseCase.login(request.email(), request.password());
    setAuthCookies(response, result);
    UserView view = currentUserQuery.getById(result.userId());
    return ResponseEntity.ok(UserResponse.from(view));
  }

  @PostMapping("/refresh")
  ResponseEntity<Void> refresh(
      @CookieValue(AuthCookies.REFRESH_TOKEN_COOKIE) String refreshToken, HttpServletResponse response) {
    AuthResult result = refreshTokenUseCase.refresh(refreshToken);
    setAuthCookies(response, result);
    return ResponseEntity.noContent().build();
  }

  private void setAuthCookies(HttpServletResponse response, AuthResult result) {
    response.addHeader(HttpHeaders.SET_COOKIE, cookies.accessTokenCookie(result.accessToken()).toString());
    response.addHeader(HttpHeaders.SET_COOKIE, cookies.refreshTokenCookie(result.refreshToken()).toString());
  }
}
