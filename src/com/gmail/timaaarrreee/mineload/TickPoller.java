package com.gmail.timaaarrreee.mineload;

import java.util.logging.Level;

public class TickPoller implements Runnable {

  private MineloadPlugin plugin;
  private long lastPoll = System.currentTimeMillis() - 3000;

  public TickPoller(MineloadPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    long now = System.currentTimeMillis();
    long timeSpent = (now - lastPoll) / 1000;
    //avoid division by zero
    if (timeSpent == 0) {
      timeSpent = 1;
    }
    //float tps = plugin.interval/timeSpent;
    float tps = 40 / timeSpent;
    //TODO finish.
    lastPoll = now;
  }
}
