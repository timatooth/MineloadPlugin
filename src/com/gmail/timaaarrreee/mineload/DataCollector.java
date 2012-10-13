package com.gmail.timaaarrreee.mineload;

import com.webkonsept.minecraft.lagmeter.LagMeter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DataCollector {

  private float tps;
  private int playerCount;
  private int maxPlayers;
  private int serverPort;
  private String serverAddress;
  private ArrayList<Player> players = new ArrayList<Player>();
  private ArrayList<Plugin> plugins = new ArrayList<Plugin>();
  private ArrayList<World> worlds = new ArrayList<World>();
  private Server server;
  private long memUsed;
  private long memMax;
  private long totalPlayers; //all the players that have ever joined
  private String motd;

  /**
   * Populates all the data fields with the state of the server.
   */
  public DataCollector() {
    server = Bukkit.getServer();
    maxPlayers = server.getMaxPlayers();
    serverPort = server.getPort();
    serverAddress = Bukkit.getServerName();
    motd = server.getMotd();
    loadPlugins();

  }

  public void update() {
    loadTPS();
    loadPlayers();
    loadMemory();
    loadWorlds();
    playerCount = server.getOnlinePlayers().length;
  }

  /**
   * Sync method called every "poll.time" ticks by the bukkit thread. It
   * collects the data to be presented to the server socket.
   */
  /**
   * Uses the LagMeter plugin to get the Ticks Per Second
   *
   * @return
   */
  private void loadTPS() {
    try {
      LagMeter lm;
      lm = (LagMeter) Bukkit.getServer().getPluginManager().getPlugin("LagMeter");
      tps = lm.getTPS();
    } catch (Exception e) {
      Bukkit.getLogger().log(Level.WARNING, "Could not get TPS. Is LagMeter installed?");
    }

  }

  /**
   * fills up array list of each players name.
   */
  private void loadPlayers() {
    players.clear();
    players.addAll(Arrays.asList(server.getOnlinePlayers()));

    //update total ever joined
    totalPlayers = server.getOfflinePlayers().length;
  }

  private void loadPlugins() {
    plugins.clear();
    plugins.addAll(Arrays.asList(server.getPluginManager().getPlugins()));
  }

  private void loadMemory() {
    memUsed = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
    memMax = Runtime.getRuntime().maxMemory() / 1048576;
  }

  private void loadWorlds() {
    worlds.clear();
    List<World> worldList = server.getWorlds();
    for (World w : worldList) {
      worlds.add(w);
    }
  }

  public ArrayList<Player> getPlayers() {
    return players;
  }

  public ArrayList<Plugin> getPlugins() {
    return plugins;
  }

  public ArrayList<World> getWorlds() {
    return worlds;
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
}
