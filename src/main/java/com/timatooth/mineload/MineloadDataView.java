package com.timatooth.mineload;

import com.alecgorge.minecraft.jsonapi.JSONAPI;
import com.timatooth.mineload.http.*;
import java.sql.*;
import org.bukkit.Bukkit;
import org.json.simple.JSONObject;

/**
 * Class handles requests that deal with obtaining data.
 * @author Tim Sullivan
 * @since MineloadPlugin 0.0.8
 */
public class MineloadDataView implements View {

  @Override
  public Response handle(Request request) {
    Session session = request.getSession();
    if(session == null){
      return Response.compose(request, "User not authenticated to retrieve data.");
    }
    
    /*
     * Ok herp derp ramble ramble. General idea:
     * - Check that user's session is valid.
     * - Lookup user in group table. Get the group's associated jsonapi user.
     * - Return jsonapi credentials as JSON object.
     */
    if(request.e("getjsonapi")){
      if(!session.getValue("mineload_authed").equals("true")){
        return Response.compose(request, "User session not active.");
      }
      //fetch jsonapi group associated with., todo
      /*
      Connection con = HttpServer.getDB().getConnection();
      try {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM ml_groups"
                + "WHERE group_id = ?");
      } catch(SQLException se){
        se.printStackTrace();
      }
      JSONAPI json = (JSONAPI) Bukkit.getServer().getPluginManager().getPlugin("JSONAPI");
      json.getJSONServer().getLogins().getUser("tim").
      * */
      JSONObject jo = new JSONObject();
      jo.put("host", "localhost");
      jo.put("port", 20059);
      jo.put("username", "admin");
      jo.put("password", "jsonpass");
      jo.put("salt", "");
      return Response.compose(request, jo.toJSONString());
    }
    
    if(request.e("history")){
      //return dataset of performance history
    }
    
    return Response.compose(request, "Invalid data request");
  }
  
}
