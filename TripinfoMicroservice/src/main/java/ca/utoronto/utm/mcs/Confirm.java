package ca.utoronto.utm.mcs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONException;
import org.json.JSONObject;

public class Confirm extends Endpoint {
  /**
   * POST /trip/confirm/
   * 
   * @body uid, radius
   * @return 200, 400, 404, 500 Add a trip to the database, containing the user's
   *         id, the driver's id, and the time at which the trip begins.
   */
  public void handlePost(HttpExchange r) throws IOException, JSONException {
    JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

    if (!body.has("driver") || !body.has("passenger") || !body.has("startTime")) {
      this.sendStatus(r, 400);
      return;
    }

    final String driver = body.getString("driver");
    final String passenger = body.getString("passenger");
    final long startTime = body.getLong("startTime");

    try {
      JSONObject newTrip = this.dao.initTrip(driver, passenger, startTime);
      this.sendResponse(r, newTrip, 200);
    } catch (Exception e) {
      this.sendStatus(r, 500);
    }
  }
}
