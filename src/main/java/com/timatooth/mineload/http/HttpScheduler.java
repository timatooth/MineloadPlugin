package com.timatooth.mineload.http;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maintains list of registered pattern/views.
 * @author Tim Sullivan
 */
public class HttpScheduler {
  
  /* View struct to associate url patterns with views */
  private Map<Pattern, View> views;

  public HttpScheduler() {
    views = new LinkedHashMap<Pattern, View>();
  }

  /**
   * Add a view to the scheduler. Will return true on success. Each pattern must
   * be unique. Will try in order of priority.
   *
   * @param urlpattern url pattern to register view for.
   * @param view reference to object implementing View.
   * @return true on success.
   */
  public synchronized boolean registerView(Pattern urlpattern, View view) {
    if (!views.containsKey(urlpattern)) {
      views.put(urlpattern, view);
      return true;
    }
    return false;
  }
  
  /**
   * Called Internally. Searches for the view associated with a URL.
   *
   * @param url to match with view.
   * @return response object. can be a response or error
   */
  public Response runView(Request request) {
    Set<Pattern> urlpatterns = views.keySet();
    for (Iterator<Pattern> it = urlpatterns.iterator(); it.hasNext();) {
      Pattern pat = it.next();
      Matcher match = pat.matcher(request.getUrl());
      if (match.matches()) {
        View matchedView = views.get(pat);
        //call view!
        request.setPattern(pat);
        if(match.groupCount() > 0){
          request.setUrl(match.group(1));
        } else {
          request.setUrl("");
        }
        return matchedView.handle(request);
      }
    }
    //no view found, its a 404 error.
    Response error = Response.compose(request, "<h1>Not Found</h1>"
            + "<p>The URL didn't match any registered mineload views</p>");
    error.setStatus(404, "Not Found");
    return error;
  }
}
