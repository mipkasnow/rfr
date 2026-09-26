package io.student.rangiffler.controller;

import io.student.rangiffler.model.types.Country;
import io.student.rangiffler.service.api.CountryService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class CountryQueryController {

  private final CountryService countryService;

  public CountryQueryController(CountryService countryService) {
    this.countryService = countryService;
  }

  @QueryMapping
  public List<Country> countries() {
    return countryService.getAllCountries();
  }
}
