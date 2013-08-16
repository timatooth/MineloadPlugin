package com.timatooth.mineload;

import com.timatooth.mineload.http.Request;
import com.timatooth.mineload.http.AssetManager;
import com.timatooth.mineload.http.Response;
import com.timatooth.mineload.http.View;
import java.io.FileNotFoundException;

/**
 * Official Internal Mineload Web Interface View.
 * handles requests for /admin/ to display dashboard.
 * @author tim
 */
public class MineloadWebView implements View {

  @Override
  public Response handle(Request request) {
    System.out.println(request);
    AssetManager am = new AssetManager("mineload");
    Response response = Response.compose(request, "Default");
    try {
      System.out.println(request.getUrl());
      response.setContent(am.loadAsset(request.getUrl().substring(1)));
      String[] chunks = request.getUrl().split("\\.");
      response.getHeaders().put("Content-Type", AssetManager.MIME.get(chunks[chunks.length-1]));
      response.getHeaders().put("Content-Length", String.valueOf(response.getContent().length));
      
    } catch (FileNotFoundException fnf){
      response = Response.compose(request, "File not found");
      response.setStatus(404, "File not found");
    }
    
    return response;
  }
  
}
