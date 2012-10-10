package com.gmail.timaaarrreee.mineload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author tim
 */
class HttpThread implements Runnable {

  private Socket server;
  private String line, input;

  public HttpThread(Socket server) {
    this.server = server;
    //Bukkit.getLogger().log(Level.INFO, "Got Mineload connection from: {0}", server.getInetAddress());
  }

  @Override
  public void run() {

    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
      PrintStream out = new PrintStream(server.getOutputStream());
      String message = MineloadPlugin.getXmlData();
      while ((line = in.readLine()) != null) {
        if (line.substring(0, 3).equalsIgnoreCase("get")) {
          if (message != null) {
            out.println("HTTP/1.1 200 OK");
            out.println("Cache-Control: no-cache");
            out.println("Content-Length: " + message.length());
            out.println("Content-Type: text/xml");
            out.println("Server: MineloadPlugin (" + Bukkit.getBukkitVersion() + ")");
            out.println();
            out.println(message);
            server.close();
          }
          else{
            out.println("HTTP/1.1 503 Service Unavailable");
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
}
