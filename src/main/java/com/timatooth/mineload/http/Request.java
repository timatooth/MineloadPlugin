package com.timatooth.mineload.http;

import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpRequests are generated from new client connections. 
 * They contain, GET, POST, cookie information.
 *
 * @author Tim Sullivan
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
  private Socket connection;
  
  /**
   * Requests are generated when a HTTP request is made.
   * @param type either GET/POST supported
   * @param url requested by user agent
   * @param httpVersion http version client supports most likely HTTP/1.1 99.99%
   * @param connection reference to active socket connection with user agent
   */
  public Request(String type, String url, String httpVersion, Socket connection) {
    this.requestType = type;
    this.url = url;
    this.httpVersion = httpVersion;
    
    parseGet();
  }

  /**
   * Get the GET parameters from agent.
   *
   * @return Map of get query.
   */
  public Map<String, String> get() {
    return this.get;
  }

  /**
   * Get the POST parameters from agent's request.
   *
   * @return Map of the POST data. Binary data is base64 encoded.
   */
  public Map<String, String> post() {
    return this.post;
  }

  /**
   * Set the Http headers for the request. Cookie maps, get/post data is set.
   *
   * @param headers
   */
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
    parseCookies();
  }
  /**
   * Process the cookie header from agent and populate map of values.
   */
  private void parseCookies() {
  }
  
  /**
   * Set the remote IP address of agent.
   * @param add 
   */
  public void setRemoteAddr(String add) {
    this.remoteAddr = add;
  }
  
  /**
   * Get the request type eg GET or POST
   * @return String either "GET" or "POST"
   */
  public String getType() {
    return this.requestType;
  }

  /**
   * Parse the GET parameters in the request.
   * populate hash map of values
   */
  private void parseGet() {
    this.get = new HashMap<String, String>();
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
    
    //remove the get query from the url
    this.url = this.url.split("\\?")[0];
  }

  /**
   * Prints information about the request.
   *
   * @return String information of request
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=HTTP Request Object=\n").append(this.url);
    sb.append("\n=Method=\n").append(this.requestType);
    sb.append("\n=Headers=\n").append(this.headers.toString());
    sb.append("\n=From=\n").append(this.remoteAddr);

    return sb.toString();
  }
  
  /**
   * URL of the request
   * @return string of url including slashes.
   */
  public String getUrl(){
    return this.url;
  }
  /**
   * Set remote socket address of user agent to the request.
   * @param socket 
   */
  public void setSocket(Socket socket){
    this.connection = socket;
  }
  
  /**
   * Get the TCP socket connected to the user agent.
   * @return Socket to user agent.
   */
  public Socket getSocket(){
    return this.connection;
  }
  
  /**
   * Get value from HTTP headers.
   * @param header header key to obtain.
   * @return value stored at key or null.
   */
  public String getHeader(String header){
    return this.headers.get(header);
  }
  
  /**
   * Set the POST data for the request object.
   * Called by Runner thread when reading incoming POST data.
   * @param post map of POST data.
   */
  public void setPost(Map<String, String> post){
    this.post = post;
  }
}
