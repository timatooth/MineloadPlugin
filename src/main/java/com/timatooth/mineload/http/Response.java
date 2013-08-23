package com.timatooth.mineload.http;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Response objects are sent to the client over a connection. They contain
 * certain header information, content, set cookies etc.
 *
 * @author Tim Sullivan
 */
public class Response {

  /* Header information to deliverTheBusiness back */
  protected Map<String, String> headers;
  /* Raw content to be sent */
  protected byte[] content;
  /* Context of response stored in request */
  protected Request request;
  /* Default response code */
  protected int code = 200;
  /* Status of response */
  protected String status = "OK";
  /* Storage of sessssions to be sent */
  protected Session session;

  /**
   * Create a Response to sent back to user agent.
   *
   * @param request Contains context including remote connection.
   * @param content Raw data to sent back in the Body or the response.
   */
  public Response(Request request, byte[] content) {
    this.request = request;
    this.content = content;
    this.headers = new HashMap<String, String>();
    this.headers.put("Content-Type", "text/html");
    this.headers.put("Server", "MineloadHTTPD");
  }

  /**
   * Create a Response to sent back to user agent.
   *
   * @param request Contains context including remote connection.
   * @param content Raw data to sent back in the Body or the response.
   */
  public Response(Request request) {
    this.request = request;
    this.content = new byte[0];
    this.headers = new HashMap<String, String>();
    this.headers.put("Content-Type", "text/html");
    this.headers.put("Server", "MineloadHTTPD");
  }

  /**
   * Set the code and status of http response. By default it's 200 OK.
   *
   * @param code HTTP code to deliverTheBusiness back eg 404.
   * @param status Associated message with error code.
   */
  public void setStatus(int code, String status) {
    this.code = code;
    this.status = status;
  }

  /**
   * Send the response to the client. Called by runner thread.
   */
  public void deliverTheBusiness() {
    /*session adding */
    if (this.session != null) {
      this.setHeader("Set-Cookie", "mineloadSessionID="+session);
    }
    
    this.setHeader("Content-Length", String.valueOf(this.content.length));
    try {
      OutputStream os = request.getSocket().getOutputStream();
      BufferedOutputStream bos = new BufferedOutputStream(os);
      bos.write(("HTTP/1.1 " + code + " " + status + "\r\n").getBytes());
      Set<String> headerKeys = headers.keySet();

      for (String key : headerKeys) {
        bos.write((key + ": " + headers.get(key) + "\r\n").getBytes());
      }

      bos.write("\r\n".getBytes());
      for (byte b : this.content) {
        bos.write(b);
      }

      bos.flush();

    } catch (IOException ioe) {
      System.out.println("Mineload: Could not get output stream to send HTTP response to " + request.getSocket().getInetAddress());
    }

  }

  /**
   * Set the raw bytes to be contained in the response.
   *
   * @param content bytes of content to be sent.
   */
  public void setContent(byte[] content) {
    this.content = content;
  }

  public void setContent(String content) {
    this.content = content.getBytes();
  }

  /**
   * Get the content of the HTTP response in raw byte form.
   *
   * @return byte[] array of response.
   */
  public byte[] getContent() {
    return this.content;
  }

  /**
   * Set the value of a header. E.g Content-Type Note: No not include the colon
   * (':') character.
   *
   * @param header Key name of header
   * @param value Value of the header
   */
  public void setHeader(String header, String value) {
    if (header.equals("Location")) {
      this.setStatus(302, "Moved Permanently");
    }
    this.headers.put(header, value);
  }

  /**
   * Helper method to build a response from a string. Default Content-Type is
   * text/html.
   *
   * @param request Contains context of the http conversation.
   * @param content Actual content to deliverTheBusiness back as string.
   * @return new Response object.
   */
  public static Response compose(Request request, String content) {
    byte[] bytes = content.getBytes();
    Response theResponse = new Response(request, bytes);
    return theResponse;
  }

  public static Response sendFile(Request request, String filename) {
    Response response = new Response(request);
    try {
      response.setContent(AssetManager.loadAsset(filename));
      String[] chunks = filename.split("\\.");
      response.setHeader("Content-Type", AssetManager.MIME.get(chunks[chunks.length - 1]));

    } catch (FileNotFoundException fnf) {
      response = Response.compose(request, "<h1>File not found</h1>"
              + "<marquee>404</marquee>"
              + "<p>Very sorry about that but it's the brutal truth of the matter.</p>"
              + "<p>P.s Also very sorry about using the &lt;marquee&gt; tag.</p>");
      response.setStatus(404, "File not found");
    }
    return response;
  }

  /**
   * Add a session to be sent with the response.
   *
   * @param sesion new session to be sent.
   */
  public void addSession(Session session) {
    this.session = session;
  }
}
