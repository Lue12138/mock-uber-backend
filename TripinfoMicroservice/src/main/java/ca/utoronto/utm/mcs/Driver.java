package ca.utoronto.utm.mcs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Driver extends Endpoint {
  /**
   * GET /trip/driver/:uid
   * 
   * @return 200, 400, 404, 500 Return a list of all trips that the driver with
   *         the given id has.
   */
  public void handleGet(HttpExchange r) throws IOException, JSONException {
    // Get the driver uid from the request
    final String uid = r.getRequestURI().getPath().split("/")[3];
    if (uid == null) {
      this.sendStatus(r, 400);
    }

    // Get the trip list from the database
    JSONArray trips = this.dao.getDriverTrips(uid);

    // Return the trip list
    JSONObject response = new JSONObject();
    response.put("data", trips);
    this.sendResponse(r, response, 200);
  }
}
