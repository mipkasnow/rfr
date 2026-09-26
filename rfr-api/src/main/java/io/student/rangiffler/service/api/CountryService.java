package io.student.rangiffler.service.api;

import io.student.rangiffler.model.types.Country;

import java.util.List;

public interface CountryService {

  List<Country> getAllCountries();

  Country getByCode(String code);
}
