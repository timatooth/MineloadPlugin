/**
 * This class aims to collect network bytes transmitted, received and the rates
 * in KB/s for each platform.
 * @author Tim Sullivan
 */
package com.gmail.timaaarrreee.mineload;

import java.io.*;
import java.util.StringTokenizer;

public class NetworkData {

  private long transmitted;
  private long received;
  private int txRate;
  private int rxRate;

  public NetworkData() {
    String os = System.getProperty("os.name").toLowerCase();
    if (isWindows(os)) {
      //grr
      processWindows();
    } else if (isLinux(os)) {
      //yay
      processLinux();
    } else if (isMac(os)) {
      //grr
      processMac();
    } else {
      //wtf...
      transmitted = -1;
      received = -1;
      rxRate = -1;
      txRate = -1;
    }
  }

  private void processWindows() {
  }

  private void processMac() {
    
    String result = cmdExec("netstat -ib");
    String[] lines = result.split("\n");
    for (int i = 0; i < lines.length; i++) {
      StringTokenizer st = new StringTokenizer(lines[i]);
      //ignore the first line (contains column names)
      if(i > 0){
        String[] data = new String[11];
        int x = 0;
        while(st.hasMoreTokens()){
          data[x] = st.nextToken();
          x++;
        }
        
        if(data[0].equals("en0")){
          System.out.println("Got ethernet interface. Bytes In: " + data[6] + " Bytess out: " + data[9]);
        } else if (data[0].equals("en1")){
          System.out.println("Got wireless interface. Bytes In: " + data[6] + " Bytess out: " + data[9]);
        }
      }
    }
  }

  private void processLinux() {
  }

  private boolean isWindows(String os) {
    return (os.indexOf("win") >= 0);
  }

  private boolean isMac(String os) {
    return (os.indexOf("mac") >= 0);
  }

  private boolean isLinux(String os) {
    return (os.indexOf("linux") >= 0);
  }

  private String cmdExec(String cmdLine) {
    String line;
    String output = "";
    try {
      Process p = Runtime.getRuntime().exec(cmdLine);
      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      while ((line = input.readLine()) != null) {
        output += (line + '\n');
      }
      input.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return output;
  }
}
