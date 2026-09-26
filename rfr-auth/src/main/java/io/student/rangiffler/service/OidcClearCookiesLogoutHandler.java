package io.student.rangiffler.service;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import java.io.IOException;

public class OidcClearCookiesLogoutHandler implements AuthenticationSuccessHandler {

  private final LogoutHandler cookieHandler;
  private final AuthenticationSuccessHandler delegate;

  public OidcClearCookiesLogoutHandler(String... cookieNames) {
    this.cookieHandler = new CookieClearingLogoutHandler(cookieNames);
    this.delegate = new OidcLogoutAuthenticationSuccessHandler();
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    this.cookieHandler.logout(request, response, authentication);
    this.delegate.onAuthenticationSuccess(request, response, authentication);
  }
}
