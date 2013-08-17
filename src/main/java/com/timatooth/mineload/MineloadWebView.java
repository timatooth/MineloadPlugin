package com.timatooth.mineload;

import com.timatooth.mineload.http.Request;
import com.timatooth.mineload.http.AssetManager;
import com.timatooth.mineload.http.Response;
import com.timatooth.mineload.http.Session;
import com.timatooth.mineload.http.View;
import java.io.FileNotFoundException;

/**
 * Official Internal Mineload Web Interface View.
 * handles requests for /mineload/ to display dashboard.
 * API needs refactoring.
 * 
 * @author Tim Sullivan
 */
public class MineloadWebView implements View {

  @Override
  public Response handle(Request request) {
    System.out.println(request);
    Session sesh = new Session();
    AssetManager am = new AssetManager("mineload"); //apibad
    Response response = Response.compose(request, "Default"); //apibad
    try {
      System.out.println(request.getUrl());
      response.setContent(am.loadAsset(request.getUrl().substring(1)));
      String[] chunks = request.getUrl().split("\\.");
      response.getHeaders().put("Content-Type", AssetManager.MIME.get(chunks[chunks.length-1]));
      response.getHeaders().put("Content-Length", String.valueOf(response.getContent().length));
      response.getHeaders().put("Set-Cookie", "mineloadSession="+sesh);
      
    } catch (FileNotFoundException fnf){
      response = Response.compose(request, "File not found");
      response.setStatus(404, "File not found");
    }
    
    return response;
  }
  
}
