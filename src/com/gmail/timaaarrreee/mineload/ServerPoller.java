
package com.gmail.timaaarrreee.mineload;

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
