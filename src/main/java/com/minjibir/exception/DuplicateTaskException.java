package com.minjibir.exception;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class DuplicateTaskException extends ClientErrorException {

   public DuplicateTaskException(String message) {
      super(message, Response.Status.CONFLICT);
   }
}
