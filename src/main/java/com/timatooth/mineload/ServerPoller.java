package com.timatooth.mineload;

/**
 * Polls server re populating the data collector.
 * @author Tim Sullivan
 */
public class ServerPoller implements Runnable{
  private DataCollector dataCollector;
  
  public ServerPoller(DataCollector dc){
    dataCollector = dc;
  }
  
  @Override
  public void run() {
    dataCollector.update();
  }
  
  
}
