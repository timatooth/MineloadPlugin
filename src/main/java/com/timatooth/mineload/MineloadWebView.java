package com.timatooth.mineload;

import com.timatooth.mineload.http.*;
import java.sql.*;

/**
 * Official Internal Mineload Web Interface View. Handles requests for
 * /mineload/ to display dashboard. API still needs refactoring and improvement.
 *
 * @author Tim Sullivan
 */
public class MineloadWebView implements View {

  @Override
  public Response handle(Request request) {
    System.out.println("MineloadWebView handle() called.");
    System.out.println(request);
    Response response = new Response(request);
    if (request.e("")) {
      if (false && request.getSession() == null) { //if they aren't logged in.
        response.setHeader("Location", "login/");
      } else { //display the panel!
        response = Response.sendFile(request, "Mineload/admin.html");
      }
    } else if (request.e("login")) { //requesting /login 
      if (request.post("loginform") != null) { // form submitted, check credentials
        String username = request.post("loginform");
        String password = request.post("passwordform");
        Connection con = HttpServer.getDB().getConnection();
        try {
          PreparedStatement ps = con.prepareStatement("SELECT * FROM ml_users"
                  + " WHERE username = ? AND password = ?");
          ps.setString(1, username);
          ps.setString(2, password);
          ResultSet rs = ps.executeQuery();
          int c = 0;
          while (rs.next()) {
            c++;
          }
          if (c == 1) {
            //password valid create session.
            Session session = new Session();
            session.setValue("username", username);
            session.setValue("valid", "true");
            response.addSession(session);
            session.setValue("mineload_authed", "true");
            session.setValue("mineload_username", username);
            response.setHeader("Location", "admin");
          } else {
            response.setHeader("Location", "login?password=wrong");
          }
        } catch (SQLException se) {
          response.setContent("Something bad happened");
          response.setStatus(500, "Internal Server Error");
          se.printStackTrace();
        }
      } else {
        //not sending POST, display form
        response = Response.sendFile(request, "Mineload/login.html");
      }
    } else if (request.e("admin")) { //requesting /admin
      if(request.getSession() != null && request.getSession().getValue("mineload_authed").equals("true")){
        response = Response.sendFile(request, "Mineload/admin.html");
      }
    } else {
      response = Response.sendFile(request, "Mineload/" + request.getUrl());
    }
    return response;
  }
}
