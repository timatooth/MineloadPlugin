package com.timatooth.mineload;

import com.timatooth.mineload.http.Request;
import com.timatooth.mineload.http.Response;
import com.timatooth.mineload.http.View;

/**
 * View to load XML data. Is called when "/" is navigated to.
 *
 * @author tim
 */
public class MineloadXmlView implements View {

  @Override
  public Response handle(Request request) {
    boolean authed;
    
    //if password is disabled
    authed = MineloadPlugin.getPassword().equals("");
    if (!authed && request.get().containsKey("password")) {
      authed = request.get().get("password").equals(MineloadPlugin.getPassword());
    }
    
    if (authed) {
      String message = new XmlFeed().getXmlData();
      Response response = Response.compose(request, message);
      response.setHeader("Content-Type", "application/xml;charset=utf-8");
      return response;
    }
    
    String message = "Invalid XML password.";
    Response err = Response.compose(request, message);
    err.setStatus(406, "Not Acceptable");
    return err;
  }
}
