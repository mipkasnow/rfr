package io.student.rangiffler.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Authorization Server authenticates public clients (client_authentication_method = none)
 * only on the token endpoint with PKCE. RFC 7009 allows a public client to identify itself
 * with client_id on the revocation endpoint, which this converter enables.
 */
public final class PublicClientRevocationAuthenticationConverter implements AuthenticationConverter {

  static final String REVOCATION_ENDPOINT = "/oauth2/revoke";

  @Override
  public Authentication convert(HttpServletRequest request) {
    if (!HttpMethod.POST.matches(request.getMethod())
        || !(request.getContextPath() + REVOCATION_ENDPOINT).equals(request.getRequestURI())
        || request.getHeader(HttpHeaders.AUTHORIZATION) != null
        || request.getParameter(OAuth2ParameterNames.CLIENT_SECRET) != null) {
      return null;
    }
    String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
    if (clientId == null || clientId.isBlank()) {
      return null;
    }
    Map<String, Object> additionalParameters = new HashMap<>();
    request.getParameterMap().forEach((name, values) -> {
      if (!OAuth2ParameterNames.CLIENT_ID.equals(name) && values.length > 0) {
        additionalParameters.put(name, values[0]);
      }
    });
    return new OAuth2ClientAuthenticationToken(
        clientId, ClientAuthenticationMethod.NONE, null, additionalParameters);
  }
}
