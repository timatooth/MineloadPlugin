package com.timatooth.mineload;

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
   */
  public void update() {
    tps = plugin.getTickPoller().getAverageTPS();
    loadMemory();
    playerCount = server.getOnlinePlayers().length;
    network.update();
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
}
