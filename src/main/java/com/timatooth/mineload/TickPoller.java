package com.timatooth.mineload;

import java.util.LinkedList;

public class TickPoller implements Runnable {
  
  /* History of ticks to calculate average */
  private LinkedList<Float> history = new LinkedList<Float>();
  /* Last time server time was polled */
  private long lastPoll = System.currentTimeMillis();
  
  /**
   * Keeping a running average of the servers tickrate performance.
   */
  @Override
  public void run() {
    final long currentTime = System.currentTimeMillis();
    long timeSpent = (currentTime - lastPoll) / 1000;
    if (timeSpent == 0) {
      timeSpent = 1;
    }
    if (history.size() > 10) {
      history.remove();
    }
    final float tps = 100f / timeSpent;
    if (tps <= 20) {
      history.add(tps);
    }
    lastPoll = currentTime;
  }
  
  /**
   * Get the average tickrate the server is running at from the main thread.
   * @return float of average tickrate.
   */
  public float getAverageTPS() {
    float avg = 0;
    for (Float f : history) {
      if (f != null) {
        avg += f;
      }
    }
    return avg / history.size();
  }
}
