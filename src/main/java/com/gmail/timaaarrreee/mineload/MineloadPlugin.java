package com.gmail.timaaarrreee.mineload;

import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.metrics.Metrics;

public class MineloadPlugin extends JavaPlugin {
  private static DataCollector data;
  private static String xmlData;
  private static String accessPassword;
  private SocketListener server;
  private int listenPort;
  private ServerPoller serverPoller;
  private TickPoller tickPoller;
  private static long heartBeatTime;
  private static long tickTime;
  private static boolean debug;

  @Override
  public void onEnable() {
    heartBeatTime = System.currentTimeMillis();
    loadConfig();

    accessPassword = getConfig().getString("password");
    listenPort = getConfig().getInt("socket.port");
    debug = getConfig().getBoolean("debug");
    data = new DataCollector(this);
    serverPoller = new ServerPoller(data);
    tickPoller = new TickPoller();

    getServer().getScheduler().scheduleSyncRepeatingTask(this, serverPoller, 80, 40);
    getServer().getScheduler().scheduleSyncRepeatingTask(this, tickPoller, 1, 100);
    
    getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        long old = heartBeatTime;
        heartBeatTime = System.currentTimeMillis();
        tickTime = heartBeatTime - old;
      }
    }, 1, 20);

    //start up the webserver thread.
    getLogger().log(Level.INFO, "Starting webserver thread...");
    server = new SocketListener(this, listenPort);
    server.start();
    /*
     * MCStats Plugin Metrics.
     */
    try {
      Metrics metrics = new Metrics(this);
      metrics.start();
    } catch (IOException e) {
      // Failed to submit the stats :-(
    }
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelAllTasks();
    serverPoller = null;
    tickPoller = null;
    server.disable();
  }


  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    //getConfig().addDefault("mysql.host", "localhost");
    //getConfig().addDefault("mysql.port", "3306");
    //getConfig().addDefault("mysql.username", "username");
    //getConfig().addDefault("mysql.password", "password");
    //getConfig().addDefault("mysql.database", "database");
    //getConfig().addDefault("socket.enabled", "true");
    //getConfig().addDefault("socket.address", "");
    getConfig().addDefault("socket.port", 25500);
    //getConfig().addDefault("polling.interval", "40");
    getConfig().addDefault("password", "changemenow539");
    getConfig().addDefault("debug", "false");
    saveConfig();
  }

  /**
   * Server socket thread and other services will access the server state from
   * here.
   *
   * @return
   */
  public static DataCollector getData() {
    return data;
  }

  public static void setXmlData(String data) {
    xmlData = data;
  }

  public static String getXmlData() {
    return xmlData;
  }

  public static String getPassword() {
    return accessPassword;
  }

  public TickPoller getTickPoller() {
    return tickPoller;
  }

  /**
   * Ticks per second doesn't seem very useful at alerting you when the server
   * main thread has come to a *complete* halt.
   *
   * Considering that the HttpThread doesn't die, it can compare the last time.
   */
  public static long getHeartbeatTime() {
    return heartBeatTime;
  }

  /**
   * Time it tooke to complete one tick.
   *
   * @return
   */
  public static long getTickTime() {
    return tickTime;
  }
  
  /**
   * Returns true if debugging is enabled
   * 
   * @return boolean debug
   */
  public static boolean debug(){
    return debug;
  }
}
