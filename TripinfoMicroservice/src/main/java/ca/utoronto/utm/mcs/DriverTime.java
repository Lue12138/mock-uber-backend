package ca.utoronto.utm.mcs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONException;
import org.json.JSONObject;

public class DriverTime extends Endpoint {
  /**
   * GET /trip/driverTime/:_id
   *
   * @return 200, 400, 404, 500 Get the time taken for the driver to reach the
   *         passenger.
   */
  public void handleGet(HttpExchange r) throws IOException, JSONException {
    String _id = r.getRequestURI().getPath().split("/")[3];
    if (_id == null) {
      this.sendStatus(r, 400);
    }

    JSONObject mongoResponse = this.dao.getTrip(_id);
    if (mongoResponse == null) {
      this.sendStatus(r, 404);
      return;
    }

    if (!mongoResponse.has("driver") || !mongoResponse.has("passenger")) {
      this.sendStatus(r, 500);
      return;
    }

    String driver = mongoResponse.getString("driver");
    String passenger = mongoResponse.getString("passenger");

    String uri = "http://locationmicroservice:8000/location/navigation/" + driver + "?passengerUid=" + passenger;
    JSONObject navigationResponse = this.sendRequest(r, uri, "", "GET");
    if (navigationResponse == null || !navigationResponse.has("data")) {
      this.sendStatus(r, 500);
      return;
    }

    JSONObject data = navigationResponse.getJSONObject("data");
    if (!data.has("total_time")) {
      this.sendStatus(r, 500);
      return;
    }

    JSONObject responseData = new JSONObject();
    responseData.put("data", new JSONObject().put("arrival_time", data.getString("total_time")));

    this.sendResponse(r, responseData, 200);
  }
}
