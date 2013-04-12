/**
 * This class aims to collect network bytes transmitted & received on
 * the servers network interface.
 *
 * @author Tim Sullivan
 */
package com.timatooth.mineload;

import java.io.*;
import java.util.StringTokenizer;

public class NetworkData {

  private long transmitted;
  private long received;
  private static boolean debug;

  public NetworkData() {
    debug = MineloadPlugin.debug();
    update();
  }

  private void processWindows() {
    if(debug){
      System.out.println("Windows network traffic tracking is not supported yet.");
    }
  }

  private void processMac() {
    //reset fields
    transmitted = 0;
    received = 0;
    String result = cmdExec("netstat -ib");
    if(result.length() < 1){
      System.err.println("Couldn't get network data");
      return;
    }
    String[] lines = result.split("\n");
    for (int i = 0; i <= lines.length; i++) {
      StringTokenizer st = new StringTokenizer(lines[i]);
      //ignore the first line (contains column names)
      if (i > 0) {
        String[] data = new String[11];
        int x = 0;
        while (st.hasMoreTokens()) {
          data[x] = st.nextToken();
          x++;
        }

        if (data[0].equals("en0")) {
          transmitted = Long.valueOf(data[9]);
          received = Long.valueOf(data[6]);
        } else if (data[0].equals("en1")) {
          //System.out.println("Got wireless interface. Bytes In: " + data[6] + " Bytess out: " + data[9]);
          transmitted += Long.valueOf(data[9]);
          received += Long.valueOf(data[6]);
          return;
        }
      }
    }
  }

  /**
   * Gets the output of the kernels network interface file
   * and parses it.
   */
  private void processLinux() {
    transmitted = 0;
    received = 0;
    String result;
    try{
      result = fileToString(new File("/proc/net/dev"));
    } catch (IOException ioe){
      if(debug){
        System.out.println("Mineload Debug: error opening /proc/net/dev");
        ioe.printStackTrace();
      }
      return;
    }
    String[] lines = result.split("\n");
    for (int i = 0; i < lines.length; i++) {
      StringTokenizer st = new StringTokenizer(lines[i]);
      
      //ignore the first line (contains column names)
      if (i > 1) {
        String[] data = new String[20];
        int x = 0;
        while (st.hasMoreTokens()) {
          data[x] = st.nextToken();
          x++;
        }
        String[] firstchunk = data[0].split(":");
        if(debug){
          System.out.println("Debug: parsing net segments. array length is " + firstchunk.length);
          for(int j = 0; j < data.length; ++j){
            System.out.println("j: " + j + ": " + data[j]);
          }
        }
        //ignore the loopback interface, add up all the rest.
        if (!firstchunk[0].equals("lo")) {
          received += Long.parseLong(data[1]);
          transmitted += Long.parseLong(data[9]);
        }
      }
    }
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
      System.err.println("Mineload: Error running command: " + cmdLine);
      ex.printStackTrace();
    }
    return output;
  }

  /**
   * Update data fields with new network data.
   */
  public final void update() {
    String os = System.getProperty("os.name").toLowerCase();
    if (isWindows(os)) {
      processWindows();
    } else if (isLinux(os)) {
      processLinux();
    } else if (isMac(os)) {
      processMac();
    } else {
      if(debug){
        System.out.println("Unknown OS detected, can't read network data");
        System.out.println(System.getProperty("os.name"));
      }
    }
    if(debug){
      System.out.println("Mineload Debug: Network TX: " + this.transmitted + " RX: " + this.received);
    }
  }

  public long getTx() {
    return transmitted;
  }

  public long getRx() {
    return received;
  }

  public static String fileToString(File file) throws IOException {
    int len;
    char[] chr = new char[4096];
    final StringBuffer buffer = new StringBuffer();
    final FileReader reader = new FileReader(file);
    try {
      while ((len = reader.read(chr)) > 0) {
        buffer.append(chr, 0, len);
      }
    } finally {
      reader.close();
    }
    return buffer.toString();
  }
}
