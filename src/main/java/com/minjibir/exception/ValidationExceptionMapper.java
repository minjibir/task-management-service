package com.minjibir.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

   @Override
   public Response toResponse(ConstraintViolationException exception) {
      var validationErrors = exception
         .getConstraintViolations()
         .stream()
         .map(ConstraintViolation::getMessage)
         .toList();

      return Response.status(Response.Status.BAD_REQUEST)
         .entity(new ApiErrorResponse("Invalid request body", validationErrors))
         .build();
   }

}
