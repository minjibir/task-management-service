package com.minjibir.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DuplicateTaskExceptionMapper implements ExceptionMapper<DuplicateTaskException> {

   @Override
   public jakarta.ws.rs.core.Response toResponse(DuplicateTaskException e) {
      return Response
         .status(e.getResponse().getStatus())
         .entity(new ApiErrorResponse(e.getMessage()))
         .build();
   }
}
