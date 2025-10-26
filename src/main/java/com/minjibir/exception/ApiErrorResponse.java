package com.minjibir.exception;

record ApiErrorResponse(String status, String message, Object data) {
   public ApiErrorResponse(String message) {
      this("ERROR", message, null);
   }

   public ApiErrorResponse(String message, Object data) {
      this("ERROR", message, data);
   }
}
