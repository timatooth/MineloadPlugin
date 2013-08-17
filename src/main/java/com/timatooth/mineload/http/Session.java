package com.timatooth.mineload.http;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Session information stored for each client. 
 * Contains a map of key values for storing information such as usernames,
 * logged in status etc. This information needs to be preserved on a database.
 *
 * @author tim
 */
public class Session implements Serializable {
  
  /* SessionID of user */
  private String sessionID;
  /* Data associated with session */
  private Map<String, String> sessionData;
  /* IP address of user */
  private String ipAddress;

  /**
   * Create a new session for person.
   * Session is assigned a UUID and then immediately inserted into 
   * the database.
   */
  public Session() {
    sessionID = UUID.randomUUID().toString();
    Connection con = HttpServer.getDB().getConnection();
    try{
      PreparedStatement ps = con.prepareStatement("INSERT INTO ml_sessions"
              + " (session_id) VALUES (?)");
      ps.setString(1, sessionID.toString());
      ps.executeUpdate();
    } catch(SQLException se){
      se.printStackTrace();
    }
  }
  
  public Session(String seshid){
    sessionID = seshid;
  }
  
  /**
   * Set a key/value pair associated with session in the database.
   * @param key
   * @param value 
   */
  public void setValue(String key, String value){
    Connection con = HttpServer.getDB().getConnection();
    try {
      PreparedStatement ps = con.prepareStatement("INSERT INTO ml_session_data"
              + " (session_id, key, value) VALUES (?,?,?)");
      ps.setString(1, sessionID);
      ps.setString(2, key);
      ps.setString(3, value);
      ps.executeUpdate();
    } catch(SQLException se){
      se.printStackTrace();
    }
  }

  /**
   * Searches in the database for a users existing session. returns null if not
   * found.
   *
   * @param sessionID id to find usually sent from cookie information.
   * @return Users session if found. Otherwise null.
   */
  public static Session getSession(String sessionID) {
    Connection con = HttpServer.getDB().getConnection();
    try{
      PreparedStatement ps = con.prepareStatement("SELECT * FROM ml_sessions WHERE session_id = ?");
      ps.setString(1, sessionID);
      ResultSet rs = ps.executeQuery();
      if(rs.getRow()!= 0){
        return new Session(sessionID);
      } else {
        return null;
      }
    } catch(SQLException se){
      se.printStackTrace();
    }
    return null;
  }
  
  @Override
  public String toString(){
    return this.sessionID;
  }
}
