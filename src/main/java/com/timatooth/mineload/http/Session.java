
package com.timatooth.mineload.http;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * Session information stored for each client.
 * Contains a map of key values for storing information
 * such as usernames, logged in status etc. This information
 * needs to be preserved on a database.
 * @author tim
 */
public class Session implements Serializable {
    /* SessionID of user */
    private UUID sessionID;
    /* Data associated with session */
    private Map<String, String> sessionData;
    /* IP address of user */
    private String ipAddress;
    /* Is the session enabled */
    private boolean enabled;
    
    /**
     * Create a new session for person.
     */
    public Session(){
        sessionID = UUID.randomUUID();
        enabled = true;
    }
    
    /**
     * Searches in the database for a users existing session.
     * returns null if not found.
     * @param sessionID id to find usually sent from cookie information.
     * @return Users session if found. Otherwise null.
     */
    public static Session getSession(String sessionID){
        return null;
    }
}
