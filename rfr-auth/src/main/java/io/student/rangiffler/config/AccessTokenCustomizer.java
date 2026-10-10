package io.student.rangiffler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class AccessTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

  static final String AUTHORITIES_CLAIM = "authorities";
  private static final String FACTOR_AUTHORITY_PREFIX = "FACTOR_";

  private final String apiAudience;

  public AccessTokenCustomizer(@Value("${rangiffler-api.audience}") String apiAudience) {
    this.apiAudience = apiAudience;
  }

  @Override
  public void customize(JwtEncodingContext context) {
    if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
      return;
    }
    Authentication principal = context.getPrincipal();
    List<String> authorities = principal == null ? List.of() : principal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(Objects::nonNull)
        .filter(authority -> !authority.startsWith(FACTOR_AUTHORITY_PREFIX))
        .distinct()
        .sorted()
        .toList();

    context.getClaims()
        .audience(List.of(apiAudience))
        .claim(AUTHORITIES_CLAIM, authorities);
  }
}
