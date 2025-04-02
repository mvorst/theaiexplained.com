package com.mattvorst.shared.controller;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.mattvorst.shared.exception.AuthorizationException;
import com.mattvorst.shared.exception.NoContentException;
import com.mattvorst.shared.exception.ResourceConflictException;
import com.mattvorst.shared.exception.UnauthorizedException;
import com.mattvorst.shared.exception.ValidationException;
import com.mattvorst.shared.model.error.ViewError;
import com.mattvorst.shared.model.error.ViewFieldError;
import com.mattvorst.shared.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MethodNotAllowedException;

@RestController
public class BaseRestController {
	private static final Logger log = LogManager.getLogger(BaseRestController.class);

	@Autowired private MessageSource messageSource;

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception exception) {
		String errorCode = "error.unknown";
		ViewError viewError = new ViewError(errorCode, messageSource.getMessage(errorCode, new Object[] {}, LocaleContextHolder.getLocale()));

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		log.error("Unhandled Exception", exception);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).build();
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<?> handleException(UnauthorizedException exception) {
		String errorCode = exception.getErrorCode() != null ? exception.getErrorCode() : exception.getMessage();
		ViewError viewError = new ViewError(errorCode, messageSource.getMessage(errorCode, new Object[] {}, LocaleContextHolder.getLocale()));

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		log.info("error.unauthorized", exception);

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(headers).body(exception.getMessage());
	}

	@ExceptionHandler({ AuthorizationException.class })
	public ResponseEntity<?> handleException(AuthorizationException exception) {
		String errorCode;

		if (exception.getPermission() != null) {
			errorCode = "error.permission." + exception.getPermission().toString().toLowerCase() + ".notFound";
		} else if (exception.getAuthorizationExceptionType() != null) {
			errorCode = exception.getAuthorizationExceptionType().getErrorCode();
		} else {
			errorCode = "error.not.authorized";
		}

		String errorMessage = messageSource.getMessage(errorCode, new Object[] {}, LocaleContextHolder.getLocale());

		ViewError viewError = new ViewError(errorCode, errorMessage);

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		log.info("Forbidden Request", exception);

		return ResponseEntity.status(HttpStatus.FORBIDDEN).headers(headers).build();
	}

	@ExceptionHandler(MethodNotAllowedException.class)
	public ResponseEntity<?> handleException(MethodNotAllowedException exception) {
		ViewError viewError = new ViewError("error.method.not.allowed", exception.getMessage());

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		log.warn("Method Not Allowed", exception);

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).build();
	}

	@ExceptionHandler(NoContentException.class)
	public ResponseEntity<?> noContentException(NoContentException exception) {
		ViewError viewError = new ViewError("error.no.content", exception.getMessage());

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).build();
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<?> handleValidationException(ValidationException validationException) {
		try {
			String errorCode = validationException.getMessage();
			String errorMessage;

			if (validationException.getArgs() != null) {
				errorMessage = messageSource.getMessage(errorCode, validationException.getArgs(), LocaleContextHolder.getLocale());
			} else {
				errorMessage = messageSource.getMessage(errorCode, new String[0], LocaleContextHolder.getLocale());
			}

			ViewError viewError = new ViewError(errorCode, errorMessage);

			if (!Utils.empty(validationException.getFieldErrorList())) {
				List<ViewFieldError> fieldErrorList = new ArrayList<>();

				for (FieldError fieldError : validationException.getFieldErrorList()) {
					log.info("FIELD_VALIDATION_ERROR:" + fieldError.getObjectName() + "." + fieldError.getField() + ":" + fieldError.getDefaultMessage());

					ViewFieldError viewFieldError = new ViewFieldError();
					viewFieldError.setField(fieldError.getField());
					viewFieldError.setMessage(fieldError.getDefaultMessage());
					fieldErrorList.add(viewFieldError);
				}

				viewError.setFieldErrorList(fieldErrorList);
			}

			HttpHeaders headers = new HttpHeaders();
			headers.add("x-error", Utils.gson().toJson(viewError));

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
		} catch (Exception e) {
			log.error("Validation Serialization Exception", e);
			return handleException(e);
		}
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> methodArgumentMismatch(MethodArgumentTypeMismatchException e) {
		String errorCode = "error.validation";
		String errorMessage = messageSource.getMessage(errorCode, new Object[] {}, LocaleContextHolder.getLocale());

		ViewFieldError viewFieldError = new ViewFieldError();
		viewFieldError.setField(e.getName());
		viewFieldError.setMessage(messageSource.getMessage(String.format("%s.invalid", e.getName()), new Object[] { e.getValue() }, LocaleContextHolder.getLocale()));

		ViewError viewError = new ViewError(errorCode, errorMessage);
		viewError.setFieldErrorList(List.of(viewFieldError));

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
	}

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<?> missingPathVariableException(MissingPathVariableException e) {
		String errorCode = "error.path.variable.missing";
		String errorMessage = messageSource.getMessage(errorCode, new Object[] { e.getVariableName() }, LocaleContextHolder.getLocale());

		ViewError viewError = new ViewError(errorCode, errorMessage);

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
	}

	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<?> resourceConflictException(ResourceConflictException resourceConflictException) {
		String errorCode = resourceConflictException.getMessage();
		String errorMessage = messageSource.getMessage(errorCode, new String[0], LocaleContextHolder.getLocale());

		ViewError viewError = new ViewError(errorCode, errorMessage);

		HttpHeaders headers = new HttpHeaders();
		headers.add("x-error", Utils.gson().toJson(viewError));

		return ResponseEntity.status(HttpStatus.CONFLICT).headers(headers).build();
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<?> requestBodyNotReadable(HttpMessageNotReadableException parseException) {
		String errorCode = "error.request.body.invalid";
		String errorMessage = messageSource.getMessage(errorCode, new Object[] {}, LocaleContextHolder.getLocale());

		Throwable cause = parseException.getCause();
		if (cause instanceof JsonMappingException jme) {
			if (jme.getPath() != null && jme.getPath().size() > 0) {
				String fieldName = jme.getPath().get(0).getFieldName();

				if (fieldName != null) {
					try {
						ViewError viewError = new ViewError(errorCode, errorMessage);
						ViewFieldError viewFieldError = new ViewFieldError();
						viewFieldError.setField(fieldName);
						viewFieldError.setMessage(messageSource.getMessage(String.format("error.%s.invalid", fieldName), new String[0], LocaleContextHolder.getLocale()));

						viewError.setFieldErrorList(List.of(viewFieldError));

						HttpHeaders headers = new HttpHeaders();
						headers.add("x-error", Utils.gson().toJson(viewError));

						return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).build();
					} catch (Exception e) {
						log.error("Unable to parse HttpMessageNotReadableException Exception", e);
					}
				}
			}
		}

		return handleException(parseException);
	}
}
