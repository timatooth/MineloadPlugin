package com.timatooth.mineload;

import com.timatooth.mineload.http.HttpServer;
import java.math.BigDecimal;
import java.math.MathContext;
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
    playerCount = server.getOnlinePlayers().length;
    network.update();
    commitSQL();
  }
  
  private void loadMemory() {
    memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    memMax = Runtime.getRuntime().maxMemory() / 1048576;
  }

  public float getTPS() {
    return tps;
  }

  public int getMaxPlayers() {
    return maxPlayers;

  }

  public int getPlayerCount() {
    return playerCount;
  }

  public long getMemoryUsed() {
    return memUsed;
  }

  public long getMaxMemory() {
    return memMax;
  }

  public long getTotalPlayers() {
    return totalPlayers;
  }

  public String getMotd() {
    return motd;
  }
  
  public NetworkData getNetwork(){
    return network;
  }
  
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
