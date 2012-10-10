
package com.gmail.timaaarrreee.mineload;

/**
 *
 * @author tim
 */
public class ServerPoller implements Runnable{
  private DataCollector dataCollector;
  
  public ServerPoller(DataCollector dc){
    dataCollector = dc;
  }
  
  @Override
  public void run() {
    //lazy way to update it by scrapping the original state completely
    //will figure out another way later.
    dataCollector.update();
    XmlFeed xf = new XmlFeed(dataCollector);
    MineloadPlugin.setXmlData(xf.getXmlData());
  }
  
  
}
