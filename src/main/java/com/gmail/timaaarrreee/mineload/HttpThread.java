package com.gmail.timaaarrreee.mineload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author tim
 */
class HttpThread implements Runnable {

  private Socket server;
  private String line;
  private Map<String, String> get_query = new HashMap<String, String>();
  private BufferedReader in;
  private PrintStream out;

  public HttpThread(Socket server) {
    this.server = server;
    if(MineloadPlugin.debug()){
      Bukkit.getLogger().log(Level.INFO, "Got Mineload connection from: {0}", server.getInetAddress());
    }
  }

  @Override
  public void run() {
    
    try {
      in = new BufferedReader(new InputStreamReader(server.getInputStream()));
      out = new PrintStream(server.getOutputStream());
      while ((line = in.readLine()) != null) {
        if (line.substring(0, 3).equalsIgnoreCase("get")) {
          //populate the map with keys from the request.

          try {
            loadGet(line);
          } catch (UnsupportedEncodingException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Mineload got a undecodable request from: {0}", server.getRemoteSocketAddress());
            sendError(500);
          }
          String tryPass = get_query.get("password");
          if(tryPass == null){
            tryPass = "";
          }
          if (MineloadPlugin.getPassword().equals("") || tryPass.equals(MineloadPlugin.getPassword())) {
            String message = new XmlFeed().getXmlData();
            
            if (message != null || message.length() < 1) {
              out.println("HTTP/1.1 200 OK");
              out.println("Cache-Control: no-cache");
              out.println("Content-Length: " + message.length());
              out.println("Content-Type: text/xml");
              out.println("Server: MineloadPlugin (" + Bukkit.getVersion() + ")");
              out.println();
              out.println(message);
              server.close();              
            } else {
              sendError(503);
            }            
          } else {
            sendError(406);
          }
          break;
        }
      }

      server.close();
    } catch (IOException ioe) {
      System.out.println("IOException in Mineload Http thread :( *rage* " + ioe);
      ioe.printStackTrace();
    }
  }

  /**
   * Process the GET url to populate the Array.
   *
   * @param querystring
   */
  private void loadGet(String querystring) throws UnsupportedEncodingException {
    String[] urlParts = querystring.split("\\?");
    if (urlParts.length > 1) {
      String query = urlParts[1];
      for (String param : query.split("&")) {
        String pair[] = param.split("=");
        String key = URLDecoder.decode(pair[0], "UTF-8");
        String value;
        if (pair.length > 1) {
          value = URLDecoder.decode(pair[1], "UTF-8");
          //cut off the extra HTTP/1.1 bit
          value = value.split("HTTP")[0].trim();
          get_query.put(key, value);
        }
      }
    }
  }

  /**
   * Send back client error message just like a normal HTTP server would.
   *
   * @param code - Http error code.
   */
  private void sendError(int code) {
    if(MineloadPlugin.debug()){
      Bukkit.getLogger().log(Level.WARNING, "Http Error: {0} from client {1}", new Object[]{code, server.getRemoteSocketAddress()});
    }
    switch (code) {
      case 406:
        String error = "<h1>HTTP/1.1 406 Not Acceptable</h1>"
                + "<p>Your request for Mineload data failed miserably.</p>"
                + "<p>You need to supply a valid password.</p>";
        out.println("HTTP/1.1 406 Not Acceptable");
        out.println("Cache-Control: no-cache");
        out.println("Content-Length: " + error.length());
        out.println("Content-Type: text/html");
        out.println("Server: MineloadPlugin (" + Bukkit.getBukkitVersion() + ")");
        out.println();
        out.println(error);
        break;
      case 503:
        out.println("HTTP/1.1 503 Service Unavailable");
        break;
      case 500:
        out.println("HTTP/1.1 500 Internal Server Error");
        break;
    }
    try {
      server.close();
    } catch (IOException ex) {
    }
  }
}
