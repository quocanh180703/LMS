package com.example.nhom3_tt_.exception;

import com.example.nhom3_tt_.domain.RestResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionTest {

  private final GlobalException globalException = new GlobalException();

  @Test
  void handleException_withMessage_returnsInternalServerErrorResponse() {
    ResponseEntity<RestResponse<Object>> response =
        globalException.handleException(new Exception("boom"));

    assertEquals(500, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(500, response.getBody().getStatusCode());
    assertEquals("boom", response.getBody().getMessage());
    assertEquals("Exception", response.getBody().getError());
  }

  @Test
  void handleException_withoutMessage_usesFallbackMessage() {
    ResponseEntity<RestResponse<Object>> response =
        globalException.handleException(new Exception());

    assertEquals(500, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("An unexpected error occurred, check ExceptionName", response.getBody().getMessage());
    assertEquals("Exception", response.getBody().getError());
  }

  @Test
  void handleIdException_usesErrorCode() {
    AppException exception = new AppException(ErrorCode.COURSE_NOT_FOUND);

    ResponseEntity<RestResponse<Object>> response = globalException.handleIdException(exception);

    assertEquals(404, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(404, response.getBody().getStatusCode());
    assertEquals("Course not found", response.getBody().getMessage());
    assertEquals("App Exception", response.getBody().getError());
  }

  @Test
  void handleValidationException_usesFirstFieldErrorMessage() throws Exception {
    Method method = GlobalExceptionTest.class.getDeclaredMethod("sampleMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "sample");
    bindingResult.addError(
        new org.springframework.validation.FieldError("sample", "name", "Name is required"));
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(parameter, bindingResult);

    ResponseEntity<RestResponse<Object>> response = globalException.handleValidationException(exception);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatusCode());
    assertEquals("Name is required", response.getBody().getMessage());
    assertEquals("Validate failed!", response.getBody().getError());
  }

  @Test
  void handleDataIntegrityViolationException_returnsBadRequest() {
    DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate key");

    ResponseEntity<RestResponse<Object>> response =
        globalException.handleIdDataIntegrityViolation(exception);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals("duplicate key", response.getBody().getMessage());
  }

  @Test
  void handleNotFoundException_returnsNotFound() {
    ResponseEntity<?> response = globalException.handleNotFoundException(new NotFoundException("missing"));

    assertEquals(404, response.getStatusCode().value());
    RestResponse<?> body = (RestResponse<?>) response.getBody();
    assertNotNull(body);
    assertEquals(404, body.getStatusCode());
    assertEquals("Not Found", body.getError());
    assertEquals("missing", body.getMessage());
  }

  @Test
  void handleAccessDeniedException_returnsForbidden() {
    ResponseEntity<?> response =
        globalException.handleAccessDeniedException(
            new org.springframework.security.access.AccessDeniedException("denied"));

    assertEquals(403, response.getStatusCode().value());
    RestResponse<?> body = (RestResponse<?>) response.getBody();
    assertNotNull(body);
    assertEquals(403, body.getStatusCode());
    assertEquals("Access Denied", body.getError());
    assertEquals("You don't have permission to do this action", body.getMessage());
  }

  @Test
  void handleCustomException_withKnownStatus_usesHttpStatusName() {
    ResponseEntity<?> response =
        globalException.handleCustomException(new CustomException("teapot", 418));

    assertEquals(418, response.getStatusCode().value());
    RestResponse<?> body = (RestResponse<?>) response.getBody();
    assertNotNull(body);
    assertEquals(418, body.getStatusCode());
    assertEquals("I_AM_A_TEAPOT", body.getError());
    assertEquals("teapot", body.getMessage());
  }

  @Test
  void handleCustomException_withUnknownStatus_usesFallbackName() {
    ResponseEntity<?> response =
        globalException.handleCustomException(new CustomException("custom", 599));

    assertEquals(599, response.getStatusCode().value());
    RestResponse<?> body = (RestResponse<?>) response.getBody();
    assertNotNull(body);
    assertEquals(599, body.getStatusCode());
    assertEquals("UNKNOWN_STATUS", body.getError());
    assertEquals("custom", body.getMessage());
  }

  @Test
  void handleIllegalArgumentException_returnsBadRequest() {
    ResponseEntity<?> response =
        globalException.handleIllegalArgumentException(new IllegalArgumentException("bad input"));

    assertEquals(400, response.getStatusCode().value());
    RestResponse<?> body = (RestResponse<?>) response.getBody();
    assertNotNull(body);
    assertEquals(400, body.getStatusCode());
    assertEquals("Invalid input", body.getError());
    assertEquals("bad input", body.getMessage());
  }

  @Test
  void handleHttpMessageNotReadableException_returnsBadRequest() {
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException("bad json", (Throwable) null);

    ResponseEntity<RestResponse<Object>> response =
        globalException.handleHttpMessageNotReadableException(exception);

    assertEquals(400, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatusCode());
    assertEquals("bad json", response.getBody().getMessage());
    assertEquals("HttpMessageNotReadableException", response.getBody().getError());
  }

  @SuppressWarnings("unused")
  private void sampleMethod(String value) {}
}