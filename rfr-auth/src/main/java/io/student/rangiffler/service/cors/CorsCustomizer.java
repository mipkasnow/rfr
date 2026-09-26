package io.student.rangiffler.service.cors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CorsCustomizer {

  private final String frontUri;
  private final String authUri;

  @Autowired
  public CorsCustomizer(@Value("${rangiffler-front.base-uri}") String frontUri,
                        @Value("${rangiffler-auth.base-uri}") String authUri) {
    this.frontUri = frontUri;
    this.authUri = authUri;
  }

  public Set<String> allowedOrigins() {
    return Set.of(frontUri, authUri);
  }

  public void corsCustomizer(HttpSecurity http) throws Exception {
    http.cors(customizer());
  }

  Customizer<CorsConfigurer<HttpSecurity>> customizer() {
    return c -> c.configurationSource(corsConfigurationSource());
  }

  CorsConfigurationSource corsConfigurationSource() {
    return request -> {
      CorsConfiguration cc = new CorsConfiguration();
      cc.setAllowCredentials(true);
      cc.setAllowedOrigins(new ArrayList<>(allowedOrigins()));
      cc.setAllowedHeaders(List.of("*"));
      cc.setAllowedMethods(List.of("*"));
      return cc;
    };
  }
}
