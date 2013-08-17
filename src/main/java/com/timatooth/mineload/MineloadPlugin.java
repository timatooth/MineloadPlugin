package com.timatooth.mineload;

import com.timatooth.mineload.http.HttpServer;
import com.timatooth.mineload.http.Database;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class MineloadPlugin extends JavaPlugin {
  private static DataCollector data;
  private static String xmlData;
  private static String accessPassword;
  private ServerPoller serverPoller;
  private TickPoller tickPoller;
  private static long heartBeatTime;
  private static long tickTime;
  private static boolean debug;
  private HttpServer mineloadServer;
  private Database database;

  @Override
  public void onEnable() {
    heartBeatTime = System.currentTimeMillis();
    loadConfig();

    accessPassword = getConfig().getString("password");
    if (accessPassword.equals("")) {
      getLogger().log(Level.WARNING, "Mineload XML password is an empty string.");
    }
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
    
    /*
     * MCStats Plugin Metrics.
     */
    try {
      Metrics metrics = new Metrics(this);
      metrics.start();
    } catch (IOException e) {
      // Failed to submit the stats :-(
    }

    Plugin jsonapi = getServer().getPluginManager().getPlugin("JSONAPI");
    Plugin lwc = getServer().getPluginManager().getPlugin("LWC");
    if (jsonapi == null) {
      getLogger().log(Level.WARNING, "JSONAPI plugin is not installed. Many web interface features won't work if you are using it..");
    }

    if (jsonapi != null && lwc != null) {
      LWCJsonProvider lwcjson = new LWCJsonProvider();
    } else {
      getLogger().log(Level.INFO, "LWC or JSONAPI was not found. Not adding extra methods.");
    }

    //start the MineloadHTTPD server
    mineloadServer = new HttpServer(getConfig().getInt("socket.port"));
    mineloadServer.start();

    Pattern pattern = Pattern.compile("^/mineload/?[\\w\\.\\-/]*$");
    if (!HttpServer.getScheduler().registerView(pattern, new MineloadWebView())) {
      getLogger().log(Level.INFO, "URLPattern(MineloadWebView): {0} registered unsuccessfully!", pattern);
    }

    //register XML view for original system. Keeping URL the same avoids breakage.
    Pattern rootPattern = Pattern.compile("^/$");
    if (!HttpServer.getScheduler().registerView(rootPattern, new MineloadXmlView())) {
      getLogger().log(Level.INFO, "URLPattern(MineloadXMLView): {0} registered unsuccessfully!", rootPattern);
    }
  }

  @Override
  public void onDisable() {
    getServer().getScheduler().cancelAllTasks();
    serverPoller = null;
    tickPoller = null;
    getLogger().log(Level.INFO, "Stopping MineloadHTTPD");
    mineloadServer.setRunning(false);
    mineloadServer.interrupt();
  }

  private void loadConfig() {
    getConfig().options().copyDefaults(true);
    getConfig().addDefault("database.engine", "h2");
    getConfig().addDefault("mysql.host", "localhost");
    getConfig().addDefault("mysql.port", 3306);
    getConfig().addDefault("mysql.username", "mineload");
    getConfig().addDefault("mysql.password", "mineload");
    getConfig().addDefault("mysql.database", "mineload");
    getConfig().addDefault("socket.port", 25500);
    getConfig().addDefault("password", "changemenow539");
    getConfig().addDefault("debug", false);
    this.saveConfig();
  }

  /**
   * Server socket thread and other services will access the server state from
   * here.
   *
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
   * Time in milliseconds of last hearing from main thread.
   * 
   * Considering that the HttpThread doesn't die, it can compare the last time.
   */
  public static long getHeartbeatTime() {
    return heartBeatTime;
  }

  /**
   * Time it took to complete one tick.
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
  public static boolean debug() {
    return debug;
  }
  
  public Database getDB(){
    return this.database;
  }
  
  public static MineloadPlugin getMineload(){
    return (MineloadPlugin) Bukkit.getServer().getPluginManager().getPlugin("MineloadPlugin");
  }
}
