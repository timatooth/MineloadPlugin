package com.timatooth.mineload.http;

import java.util.Map;

/**
 * Response objects are sent to the client over a connection. They contain
 * certain header information, content, setcookies etc.
 *
 * @author tim
 */
public class Response {

  /* Header information to send back */
  private Map<String, String> headers;
  /* Text content to be sent */
  private String content;
}
