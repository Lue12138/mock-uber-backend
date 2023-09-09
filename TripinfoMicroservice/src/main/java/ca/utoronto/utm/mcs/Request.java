package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.Iterator;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Request extends Endpoint {
  /**
   * POST /trip/request/
   *
   * @body uid, radius
   * @return 200, 400, 404, 500 Make a request for a trip to drivers within a
   *         given radius of the user's position.
   */
  public void handlePost(HttpExchange r) throws IOException, JSONException {
    JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

    if (!body.has("uid") || !body.has("radius")) {
      this.sendStatus(r, 400);
      return;
    }

    final String uid = body.getString("uid");
    final double radius = body.getDouble("radius");

    String uri = "http://locationmicroservice:8000/location/nearbyDriver/" + uid + "?radius=" + radius;
    JSONObject response = this.sendRequest(r, uri, "", "GET");
    if (response == null || !response.has("data")) {
      this.sendStatus(r, 500);
      return;
    }

    JSONObject data = response.getJSONObject("data");
    Iterator<String> keys = data.keys();
    JSONArray drivers = new JSONArray();
    while (keys.hasNext()) {
      drivers.put(keys.next());
    }

    JSONObject responseData = new JSONObject();
    responseData.put("data", drivers);

    this.sendResponse(r, responseData, 200);
  }
}
