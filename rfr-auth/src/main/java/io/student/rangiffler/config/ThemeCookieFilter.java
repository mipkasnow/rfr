package io.student.rangiffler.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ThemeCookieFilter extends OncePerRequestFilter {

  static final String THEME_PARAM = "theme";
  static final String THEME_COOKIE = "rangiffler-theme";
  private static final Set<String> THEMES = Set.of("light", "dark");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    String theme = request.getParameter(THEME_PARAM);
    if (theme != null && THEMES.contains(theme)) {
      response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(THEME_COOKIE, theme)
          .path("/")
          .maxAge(Duration.ofDays(365))
          .sameSite("Lax")
          .build()
          .toString());
    }
    filterChain.doFilter(request, response);
  }
}
