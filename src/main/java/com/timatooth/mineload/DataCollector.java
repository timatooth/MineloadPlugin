package com.timatooth.mineload;

import com.timatooth.mineload.http.HttpServer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class DataCollector {

  private MineloadPlugin plugin;
  private float tps;
  private int playerCount;
  private int maxPlayers;
  private Server server;
  private long memUsed;
  private long memMax;
  private long totalPlayers; //all the players that have ever joined
  private String motd;
  private NetworkData network;

  /**
   * Populates all the data fields with the state of the server.
   */
  public DataCollector(MineloadPlugin plugin) {
    this.plugin = plugin;
    server = Bukkit.getServer();
    maxPlayers = server.getMaxPlayers();
    motd = server.getMotd();
    network = new NetworkData();
  }
  
   /**
   * Sync method called every "poll.time" ticks by the bukkit thread. It
   * collects the data to be presented to the server socket.
   * possible need for a watchdog here to avoid hangs
   */
  public void update() {
    tps = plugin.getTickPoller().getAverageTPS();
    loadMemory();
    playerCount = server.getOnlinePlayers().size();
    network.update();
    commitSQL();
  }
  
  private void loadMemory() {
    memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    memMax = Runtime.getRuntime().maxMemory() / 1048576;
  }
  
  /**
   * Get the servers tick rate.
   * Should normally be 20tps.
   * @return tickrate as float
   */
  public float getTPS() {
    return tps;
  }
  
  /**
   * Maximum amount of players supported by server.
   * @return maxPlayers
   */
  public int getMaxPlayers() {
    return maxPlayers;

  }
  
  /**
   * Get current players online.
   * @return playerCount
   */
  public int getPlayerCount() {
    return playerCount;
  }
  
  /**
   * Get memory used by this Java Virtual Machine.
   * @return memUsed in MB.
   */
  public long getMemoryUsed() {
    return memUsed;
  }
  
  /**
   * Get the Maximum memory allocated to the Java Virtual Machine.
   * @return memMax in MB
   */
  public long getMaxMemory() {
    return memMax;
  }
  
  /**
   * Get total unique player count.
   * How many unique player names that have ever joined.
   * @return totalPlayers
   */
  public long getTotalPlayers() {
    return totalPlayers;
  }
  
  /**
   * Get the Message Of the Day assined from server.properties.
   * @return motd as a String.
   */
  public String getMotd() {
    return motd;
  }
  
  /**
   * Network transmission data.
   * @return NetworData object.
   */
  public NetworkData getNetwork(){
    return network;
  }
  
  /**
   * Saves data in this class to a SQL database.
   * Should be *really* be moved to a scheduler to change save rate.
   */
  private void commitSQL(){
    Connection con = HttpServer.getDB().getConnection();
    try {
      PreparedStatement ps = con.prepareStatement("INSERT INTO ml_performance ("
              + "time, playercount, memused, tps, ticktime, heartbeat, tx, rx) VALUES ("
              + "?,?,?,?,?,?,?,?)");
      ps.setTimestamp(1, new Timestamp(new Date().getTime()));
      ps.setInt(2, this.playerCount);
      ps.setInt(3, (int)((float)memUsed/memMax)*100);
      ps.setInt(4, (int)tps);
      ps.setLong(5, MineloadPlugin.getTickTime());
      long lastContactMainThread = System.currentTimeMillis() - MineloadPlugin.getHeartbeatTime();
      ps.setLong(6, lastContactMainThread);
      ps.setLong(7, network.getTx());
      ps.setLong(8, network.getRx());
      ps.executeUpdate();
    } catch(SQLException se){
      se.printStackTrace();
    }
  }
}
