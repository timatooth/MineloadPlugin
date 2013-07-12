package com.timatooth.mineload.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Response objects are sent to the client over a connection. They contain
 * certain header information, content, setcookies etc.
 *
 * @author tim
 */
public class Response {

  /* Header information to send back */
  protected Map<String, String> headers;
  /* Text content to be sent */
  protected byte[] content;
  /* Context of response stored in request */
  protected Request request;
  /* Default response code */
  protected int code = 200;
  /* Status of response */
  protected String status = "OK";

  public Response(Request request, byte[] content) {
    this.request = request;
    this.content = content;
    this.headers = new HashMap<String, String>();
    this.headers.put("Content-Type", "text/html");
    this.headers.put("Server", "MineloadHTTPD");
  }

  /**
   * Set the code and status of http response. By default it's 200 OK.
   *
   * @param code
   * @param status
   */
  public void setStatus(int code, String status) {
    this.code = code;
    this.status = status;
  }

  /**
   * Send the response to the client.
   */
  public void send() {
    try {
      OutputStream os = request.getSocket().getOutputStream();
      OutputStreamWriter w = new OutputStreamWriter(os);
      w.write("HTTP/1.1 " + code + " " + status + "\r\n");
      Set<String> headerKeys = headers.keySet();
      
      for (String key : headerKeys) {
        w.write(key + ": " + headers.get(key) + "\r\n");
      }
      
      w.write("\r\n"); //blank line
      w.flush();
      for (byte b : this.content) {
        os.write(b);
      }
      os.flush();
    } catch (IOException ioe) {
      System.out.println("Could not get output stream to send response");
    }

  }
  /**
   * Set the raw bytes of the content to be sent.
   * @param content bytes of content to be sent.
   */
  public void setContent(byte[] content) {
    this.content = content;
  }
  
  /**
   * Get the response header Map.
   * @return Map containing HTTP response headers.
   */
  public Map<String, String> getHeaders() {
    return this.headers;
  }
  
  /**
   * Get the content of the HTTP response in raw byte form.
   * @return byte[] array of response.
   */
  public byte[] getContent() {
    return this.content;
  }
  
  /**
   * Set the value of a header.
   * E.g Content-Type
   * Note: No not include the colon (':') character.
   * @param header Key name of header
   * @param value Value of the header
   */
  public void setHeader(String header, String value) {
    this.headers.put(header, value);
  }
  
  /**
   * Helper method to build a response from a string.
   * Default Content-Type is text/html.
   * @param request Contains context of the http conversation.
   * @param content Actual content to send back as string.
   * @return new Response object.
   */
  public static Response compose(Request request, String content){
    byte[] bytes = content.getBytes();
    Response theResponse = new Response(request, bytes);
    return theResponse;
  }
}
