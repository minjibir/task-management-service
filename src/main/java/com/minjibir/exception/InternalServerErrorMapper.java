package com.minjibir.exception;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class InternalServerErrorMapper implements ExceptionMapper<Exception> {

   @Inject
   Logger logger;

   @Override
   public Response toResponse(Exception exception) {
      var errorMessage = "An unexpected error occurred. Please contact the administrator.";

      logger.error(errorMessage, exception);

      return Response
         .status(Response.Status.INTERNAL_SERVER_ERROR)
         .entity(new ApiErrorResponse(errorMessage))
         .build();
   }

}
