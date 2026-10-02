package io.student.rangiffler.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Map;

/**
 * Authenticates a public client by client_id for token revocation requests produced by
 * {@link PublicClientRevocationAuthenticationConverter}. Other client authentication requests
 * are left to the default Spring Authorization Server providers.
 */
public final class PublicClientRevocationAuthenticationProvider implements AuthenticationProvider {

  private final RegisteredClientRepository registeredClientRepository;

  public PublicClientRevocationAuthenticationProvider(RegisteredClientRepository registeredClientRepository) {
    this.registeredClientRepository = registeredClientRepository;
  }

  @Override
  public Authentication authenticate(Authentication authentication) {
    OAuth2ClientAuthenticationToken clientAuthentication = (OAuth2ClientAuthenticationToken) authentication;
    if (!ClientAuthenticationMethod.NONE.equals(clientAuthentication.getClientAuthenticationMethod())
        || !isRevocationRequest(clientAuthentication.getAdditionalParameters())) {
      return null;
    }

    String clientId = clientAuthentication.getPrincipal().toString();
    RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
    if (registeredClient == null
        || !registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
      throw new OAuth2AuthenticationException(new OAuth2Error(
          OAuth2ErrorCodes.INVALID_CLIENT, "Public client authentication failed: " + OAuth2ParameterNames.CLIENT_ID, null));
    }
    return new OAuth2ClientAuthenticationToken(registeredClient, ClientAuthenticationMethod.NONE, null);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication);
  }

  private static boolean isRevocationRequest(Map<String, Object> parameters) {
    return parameters.containsKey(OAuth2ParameterNames.TOKEN)
        && !parameters.containsKey(OAuth2ParameterNames.GRANT_TYPE);
  }
}
