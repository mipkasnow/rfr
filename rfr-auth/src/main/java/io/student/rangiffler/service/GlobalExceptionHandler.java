package io.student.rangiffler.service;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final String ERROR_VIEW_NAME = "error";
  private final String frontUri;

  public GlobalExceptionHandler(@Value("${rangiffler-front.base-uri}") String frontUri) {
    this.frontUri = frontUri;
  }

  @ExceptionHandler(Exception.class)
  public String handleAnyException(Exception ex,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   Model model) {
    Object statusCodeAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int statusCode;
    if (ex instanceof NoResourceFoundException) {
      statusCode = HttpServletResponse.SC_NOT_FOUND;
    } else if (statusCodeAttr instanceof Integer) {
      statusCode = (Integer) statusCodeAttr;
    } else {
      statusCode = response.getStatus();
      if (statusCode == HttpServletResponse.SC_OK) {
        statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
      }
    }

    HttpStatus httpStatus = HttpStatus.resolve(statusCode);
    if (httpStatus == null) {
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    model.addAttribute("status", statusCode);
    model.addAttribute("error", httpStatus.getReasonPhrase());
    model.addAttribute("message", ex.getMessage());
    model.addAttribute("path", request.getRequestURI());
    model.addAttribute("frontUri", frontUri);

    response.setStatus(statusCode);
    return ERROR_VIEW_NAME;
  }
}
