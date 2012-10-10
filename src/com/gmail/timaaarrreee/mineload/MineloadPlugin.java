package com.gmail.timaaarrreee.mineload;

import com.webkonsept.minecraft.lagmeter.LagMeter;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MineloadPlugin extends JavaPlugin {
  protected LagMeter lagmeter;
  //data of current server state is stored here
  private static DataCollector data;
  private static String xmlData;
  private static String accessPassword;
  private SocketListener server;
  private int listenPort;
  
  @Override
  public void onEnable() {
    loadConfig();
    
    accessPassword = getConfig().getString("password");
    listenPort = getConfig().getInt("socket.port");
    data = new DataCollector();
    Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ServerPoller(data), 80, 40);
    
    //start up the webserver thread.
    getLogger().log(Level.INFO, "Starting webserver thread...");
    server = new SocketListener(this, listenPort);
    server.start();
  }
  
  @Override
  public void onDisable(){
    server.disable();
  }
  
  

  @Override
  public void reloadConfig() {
    super.reloadConfig();
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
    saveConfig();
  }
  
  /**
   * Server socket thread and other services will access
   * the server state from here.
   * @return 
   */
  public static DataCollector getData(){
    return data;
  }
  
  public static void setXmlData(String data){
    xmlData = data;
  }
  
  public static String getXmlData(){
    return xmlData;
  }
  
  public static String getPassword(){
    return accessPassword;
  }
  
}
