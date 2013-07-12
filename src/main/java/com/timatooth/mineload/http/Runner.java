package com.timatooth.mineload.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Does all the hard work getting POST/GET information, sending responses etc.
 * Generates Request and Response objects.
 *
 * @author tim
 */
class Runner implements Runnable {

  /*connection to client */
  private Socket connection;
  /* Data coming from the browser */
  private BufferedReader in;
  /* Data to be sent back to the browser */
  private PrintStream out;

  public Runner(Socket connection) {
    this.connection = connection;
    //set up input and output streams from connection.
    try {
      InputStream is = connection.getInputStream();
      InputStreamReader isr = new InputStreamReader(is);
      in = new BufferedReader(isr);
      out = new PrintStream(connection.getOutputStream());
    } catch (IOException ioe) {
      System.out.println("Failed setting up io streams");
      ioe.printStackTrace();
    }
  }

  /**
   * Processes new connections. Parses headers, data etc to obtain GET, POST,
   * cookie information and produces a Request object. Registered handlers deal
   * with the requests when they're generated to produce Response objects which
   * are sent back.
   */
  @Override
  public void run() {
    String line;
    int lineCount = 0;
    Request request = null;
    Map<String, String> headers = new HashMap<String, String>();
    try {
      while ((line = in.readLine()) != null) {
        if (lineCount == 0) {
          //parse the first request line.
          if ((request = parseRequest(line)) == null) {
            //malformed request
            System.out.println("Got a malformed request");
            break;
          }
        } else if (!line.isEmpty()) {
          String[] chunks = line.split(":", 2);
          if (chunks.length == 2) {
            headers.put(chunks[0], chunks[1].trim());
          }
        }

        lineCount++;

        if (line.isEmpty() && request != null) {
          request.setHeaders(headers);
          request.setRemoteAddr(connection.getRemoteSocketAddress().toString());
          request.setSocket(connection);

          Response res = HttpServer.getScheduler().runView(request);
          res.send();

          if (request.getType().equalsIgnoreCase("get")) {
            //FIXME
            break;
          }
        }
      }

      this.connection.close();

    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
   * Parse the initial request line. E.g GET / HTTP/1.1 or: POST /data HTTP/1.1
   *
   * @param line
   * @return A new request object if supported. Otherwise null.
   */
  private Request parseRequest(String line) {
    Request request = null;
    Scanner input = new Scanner(line);
    if (input.hasNext("GET")) {
      String type = input.next();
      String url = input.next();
      String httpVersion = input.next();
      request = new Request(type, url, httpVersion, connection);
      
    } else if (input.hasNext("POST")) {
      try {
        String type = input.next();
        String url = input.next();
        String httpVersion = input.next();
        request = new Request(type, url, httpVersion, connection);
      } catch (Exception e) {
      }
    } else {
      //bad request
      System.out.println("Got a bad request from "
              + connection.getRemoteSocketAddress());
    }

    return request;
  }
}
