package com.timatooth.mineload;

import com.timatooth.mineload.http.HttpServer;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

/**
 * MineloadPlugin Main class.
 * Contains main entry point onEnable().
 * @author Tim Sullivan
 */
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
  
  /**
   * Starting method
   */
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

    Pattern pattern = Pattern.compile("^/mineload/?([\\w\\.\\-/]*)$");
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
  
  /**
   * Set up default configuration options.
   */
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
   * All collected server data is collected here for convenience.
   * @return DataCollector containing all performance data.
   */
  public static DataCollector getData() {
    return data;
  }
  
  /**
   * Set the XML String to be sent back to clients.
   * @param data XML data to be set.
   */
  public static void setXmlData(String data) {
    xmlData = data;
  }
  
  /**
   * Get the performance data stored in an XML encoded String
   * @return String of XML data.
   */
  public static String getXmlData() {
    return xmlData;
  }
  
  /**
   * Get the XML access password.
   * @return 
   */
  public static String getPassword() {
    return accessPassword;
  }
  
  /**
   * Get the TickPoller which collects ticks per second.
   * @return TickPoller
   */
  public TickPoller getTickPoller() {
    return tickPoller;
  }

  /**
   * Time in milliseconds of last hearing from main thread.
   *
   * Considering that the HttpServer doesn't die, it can compare the last time.
   */
  public static long getHeartbeatTime() {
    return heartBeatTime;
  }

  /**
   * Time it took to complete one tick.
   *
   * @return tickTIme
   */
  public static long getTickTime() {
    return tickTime;
  }

  /**
   * Returns true if plugin debugging is enabled
   * @return boolean debug
   */
  public static boolean debug() {
    return debug;
  }
  
  /**
   * Get the live running instance of Mineload
   * @return MineloadPlugin
   */
  public static MineloadPlugin getMineload() {
    return (MineloadPlugin) Bukkit.getServer().getPluginManager().getPlugin("MineloadPlugin");
  }
}
