package com.timatooth.mineload.http;

/**
 * Views are called when the pattern it's registered with matches a Request.
 * Simply implement this class and register it with the HttpScheduler. Response
 * is returned to HTTP client.
 * @author tim
 */
public interface View {
  /**
   * Will be called when a request is made.
   * Must return a response object which is sent to client.
   * @return Response object with data.
   */
  public Response handle(Request request);
}
