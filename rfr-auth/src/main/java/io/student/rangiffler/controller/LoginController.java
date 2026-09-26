package io.student.rangiffler.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class LoginController {

  private static final String LOGIN_VIEW_NAME = "login";
  private static final String PRE_REQ_ATTR = "SPRING_SECURITY_SAVED_REQUEST";
  private static final String PRE_REQ_URI = "/oauth2/authorize";

  private final String frontUri;

  public LoginController(@Value("${rangiffler-front.base-uri}") String frontUri) {
    this.frontUri = frontUri;
  }

  @GetMapping("/login")
  public String login(HttpSession session) {
    return isOauthSessionContainsRedirectUri(session, frontUri)
        ? LOGIN_VIEW_NAME
        : "redirect:" + frontUri;
  }

  @GetMapping("/")
  public String root(Authentication authentication) {
    return (authentication == null || !authentication.isAuthenticated())
        ? LOGIN_VIEW_NAME
        : "redirect:" + frontUri;
  }

  private boolean isOauthSessionContainsRedirectUri(HttpSession session, String redirectUri) {
    final DefaultSavedRequest savedRequest = (DefaultSavedRequest) session.getAttribute(PRE_REQ_ATTR);
    return savedRequest != null &&
        savedRequest.getRequestURI().equals(PRE_REQ_URI) &&
        Arrays.stream(savedRequest.getParameterValues("redirect_uri")).anyMatch(url -> url.contains(redirectUri));
  }
}
