package com.timatooth.mineload.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * HttpRequests are sent from client connections. They contain, GET, POST,
 * cookie information.
 *
 * @author tim
 */
public class Request {

  /* Type of request */
  private String requestType;
  /* GET from user request */
  private Map<String, String> get;
  /* POST data from user request */
  private Map<String, String> post;
  /* All the request header information */
  private Map<String, String> headers;
  /* Cookies sent from the agent */
  private Map<String, String> cookies;
  /* url of the request */
  private String url;
  /* HTTP version of the request */
  private String httpVersion;
  /* Remote address of agent */
  private String remoteAddr;

  public Request(String type, String url, String httpVersion) {
    this.requestType = type;
    this.url = url;
    this.httpVersion = httpVersion;
  }

  /**
   * Get the GET parameters from agent.
   *
   * @return Map of get query.
   */
  public Map<String, String> get() {
    return null;
  }

  /**
   * Get the POST parameters from agent's request.
   *
   * @return Map of the POST data. Binary data is base64 encoded.
   */
  public Map<String, String> post() {
    return null;
  }

  /**
   * Set the Http headers for the request. Cookie maps, get/post data is set.
   *
   * @param headers
   */
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
    //parseGet();
    if (this.requestType.equalsIgnoreCase("post")) {
      parsePost();
    }
    parseCookies();
  }

  private void parseCookies() {
  }

  private void parsePost() {
  }

  public void setRemoteAddr(String add) {
    this.remoteAddr = add;
  }

  public String getType() {
    return this.requestType;
  }

  /**
   * Parse the GET parameters in the request.
   */
  private void parseGet() {
    String[] urlParts = url.split("\\?");
    if (urlParts.length > 1) {
      String query = urlParts[1];
      for (String param : query.split("&")) {
        String pair[] = param.split("=");
        String key = null;
        try {
          key = URLDecoder.decode(pair[0], "UTF-8");
        } catch (UnsupportedEncodingException uee) {
          uee.printStackTrace();
        }
        String value = null;
        if (pair.length > 1) {
          try {
            value = URLDecoder.decode(pair[1], "UTF-8");
          } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
          }
          this.get.put(key, value);
        }
      }
    }
  }

  /**
   * Prints information about the request.
   *
   * @return String information of request
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=HTTP Request=\n").append(this.url);
    sb.append("\n=Method=\n").append(this.requestType);
    sb.append("\n=Headers=\n").append(this.headers.toString());
    sb.append("\n=From=\n").append(this.remoteAddr);

    return sb.toString();
  }
}
