package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.io.IOException;

public class Nearby extends Endpoint{
    // TODO

    /**
     * @param centerX, centerY, X, Y, radius
     * @return true, false
     * Determine whether a point(X,Y) is within the circle defined by center(centerX,centerY) and radius.
     */
    public boolean withinCircle(double centerX, double centerY, double X, double Y, double radius) {
        return (X-centerX)*(X-centerX) + (Y-centerY)*(Y-centerY) <= radius*radius;
    }

    /**
     * GET /location/nearbyDriver/:uid?radius=
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get the drivers that are in a radius that the user defined from the
     * user’s current location.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // parse the url to get proper params
        String[] params = r.getRequestURI().getPath().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400, true);
            return;
        }
        if (r.getRequestURI().getQuery() == null) {
            this.sendStatus(r, 400, true);
            return;
        }
        params = r.getRequestURI().getQuery().split("=");
        if (params.length != 2 || params[1].isEmpty()) {
            this.sendStatus(r, 400, true);
            return;
        }
        try {
            final String uid = r.getRequestURI().getPath().split("/")[3];
            final double radius = Double.parseDouble(r.getRequestURI().getQuery().split("=")[1]);

            Result result = this.dao.getUserLocationByUid(uid);
            if (result.hasNext()) {
                JSONObject res = new JSONObject();
                Record user = result.next();
                Double centerLongitude = user.get("n.longitude").asDouble();
                Double centerLatitude = user.get("n.latitude").asDouble();

                // get all drivers
                JSONObject data = new JSONObject();
                Result driverResult = this.dao.getDriversLocation(true);
                int i = 1;
                while(driverResult.hasNext()) {
                    Record driver = driverResult.next();
                    Double driverLongitude = driver.get("n.longitude").asDouble();
                    Double driverLatitude = driver.get("n.latitude").asDouble();
                    // check if the driver is within the radius
                    if(withinCircle(centerLongitude, centerLatitude, driverLongitude, driverLatitude, radius)) {
                        // the driver is within the radius, insert driver's info into as a nested JSON

                        // initial driver's info
                        String driverStreet = driver.get("n.street").asString();
                        JSONObject driverInfo = new JSONObject();
                        driverInfo.put("longitude", driverLongitude);
                        driverInfo.put("latitude", driverLatitude);
                        driverInfo.put("street", driverStreet);

                        // insert
                        data.put(Integer.toString(i), driverInfo);
                        i++;
                    }
                }
                i = 0;
                res.put("data", data);
                res.put("status", "OK");
                this.sendResponse(r, res, 200);
                return;
            } else {
                this.sendStatus(r, 404, true);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500, true);
            return;
        }
    }
}