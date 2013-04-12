package com.timatooth.mineload;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class SocketListener extends Thread {

  private ServerSocket server;
  //to indicate the server to stop
  boolean keepRunning = true;

  public SocketListener(Plugin plugin, int listenPort) {
    this.setName("Mineload Socket Thread");
    try {
      Bukkit.getLogger().log(Level.INFO, "Starting Mineload XML Service on port {0}", listenPort);
      server = new ServerSocket(listenPort);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void run() {
    while (keepRunning) {
      try {
        Socket connection = server.accept();
        HttpThread httpInstance = new HttpThread(connection);
        Thread t = new Thread(httpInstance);
        t.start();
      } catch (SocketException se) {
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public void disable() {
    System.out.println("Mineload should be stopping...");
    keepRunning = false;
    try {
      server.close();
    } catch (IOException ex) {
    }
  }
}
