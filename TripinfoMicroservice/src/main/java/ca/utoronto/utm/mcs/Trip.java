package ca.utoronto.utm.mcs;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONException;
import org.json.JSONObject;

public class Trip extends Endpoint {
  /**
   * PATCH /trip/:_id/
   * 
   * @body distance, endTime, timeElapsed, discount, totalCost, driverPayout
   * @return 200, 400, 404, 500 Complete a trip in the database with supplemental
   *         information.
   */
  public void handlePatch(HttpExchange r) throws IOException, JSONException {
    JSONObject body = new JSONObject(Utils.convert(r.getRequestBody()));

    if (!body.has("distance") || !body.has("endTime") || !body.has("timeElapsed")
        || !body.has("discount") || !body.has("totalCost") || !body.has("driverPayout")) {
      this.sendStatus(r, 400);
      return;
    }

    final String _id = r.getRequestURI().getPath().split("/")[2];
    if (_id == null) {
      this.sendStatus(r, 400);
      return;
    }

    final int distance = body.getInt("distance");
    final long endTime = body.getLong("endTime");
    final long timeElapsed = body.getLong("timeElapsed");
    final double discount = body.getDouble("discount");
    final double totalCost = body.getDouble("totalCost");
    final double driverPayout = body.getDouble("driverPayout");

    boolean success = this.dao.completeTrip(_id, distance, endTime, timeElapsed, discount, totalCost, driverPayout);
    if (success) {
      this.sendStatus(r, 200);
    } else {
      this.sendStatus(r, 404);
    }
  }
}
