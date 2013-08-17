package com.timatooth.mineload.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class listens on a specified port accepting new connections. New connections
 * are started in a new thread.
 *
 * Ideas: - Set global limit of running connections/threads.
 *        - Use a thread pool using Executors.
 *
 * @author Tim Sullivan
 */
public class HttpServer extends Thread {

  /* Keep track of currently running HTTP instance threads */
  private int threadCount;
  /* Java server socket it's listening on */
  private ServerSocket serverSocket;
  /* Will keep running listener thread while running flag is true */
  private boolean keepRunning;
  /* Keep a thread limit to restrict system resource usage */
  private int connectionLimit;
  /* HttpScheduler stores collection of Views */
  private static HttpScheduler scheduler;
  /* SQL Database connection */
  private static Database database;

  /**
   * Create the Http HttpServer on specified port. Creates Http runtime threads
   * for each connection.
   *
   * @param port The specified port to listen on.
   */
  public HttpServer(int port) {
    this.connectionLimit = 200;
    /* set name of thread */
    this.setName("Mineload HTTP Listener Thread");
    /* set status to be a background daemon thread */
    this.setDaemon(true);
    HttpServer.scheduler = new HttpScheduler();

    try {
      serverSocket = new ServerSocket(port);
      threadCount++;
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    database = new Database();
    
    //set initial state to running.
    keepRunning = true;
  }

  /**
   * Set the state of the listener thread. HttpServer thread should terminate when
   * ready and no longer accept new connections.
   *
   * @param state set to false to disable listener.
   */
  public synchronized void setRunning(boolean state) {
    this.keepRunning = state;
    if(!state){
      try {
        this.serverSocket.close();
        try {
          database.getConnection().commit();
          database.getConnection().close();
        } catch (SQLException ex) {
          Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
      } catch (IOException ex) {
        Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, "Socket did not close cleanly.", ex);
      }
    }
  }

  /**
   * Main listener loop. Will keep running until running state is set to false.
   * Creates new http runtime threads which generate Request objects.
   */
  @Override
  public void run() {
    while (keepRunning) {
      /* Only accept new connections if limit hasn't been reached */
      if (threadCount < connectionLimit) {
        try {
          Socket connection = serverSocket.accept();
          Runner run = new Runner(connection);
          Thread newThread = new Thread(run);
          newThread.start();
        } catch (SocketException se) {
          //socket now closed due to reload.
        } catch (IOException ioe){
          ioe.printStackTrace();
        }
      }
    }
  }

  /**
   * Set the limit of how many connections are allowed to HTTP server.
   *
   * @param limit Limit to set
   */
  public synchronized void setConnectionLimit(int limit) {
    this.connectionLimit = limit;
  }

  /**
   * Called by Runner threads once they're done working. Decreases the thread
   * count to make way for new connections.
   */
  public synchronized void updateConnectionCount() {
    this.threadCount--;
  }
  
  /**
   * Get Scheduler to register new Views.
   * @return 
   */
  public static synchronized HttpScheduler getScheduler(){
    return scheduler;
  }
  
  /**
   * Get the global SQL database instance.
   * @return Database
   */
  public static Database getDB(){
    return database;
  }
}
