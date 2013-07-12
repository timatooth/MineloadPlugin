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
    String message = "<h1>Hello World</h1>"
            + "<p>I'm a view that will be displayed on the browser!</p>" 
            + "<hr>"
            + "<h2>More Data Here</h2>";
    Response response = Response.compose(request, message);
    AssetManager am = new AssetManager("mineload");
    
    try {
      response.setContent(am.loadAsset(request.getUrl().substring(1)));
      //set the content type of response
      String[] chunks = request.getUrl().split("\\.");
      //System.out.println(request.getUrl());
      response.getHeaders().put("Content-Type", AssetManager.MIME.get(chunks[chunks.length-1]));
      response.getHeaders().put("Content-Length", String.valueOf(response.getContent().length));
      
    } catch (FileNotFoundException fnf){
      //throw 404 error or try something else..
      System.out.println("Error 404 file not found");
      fnf.printStackTrace();
    }
    
    //AssetManager.rootDir.getAbsolutePath()
    return response;
  }
  
}
