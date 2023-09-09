package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.io.IOException;
import java.util.List;

public class Navigation extends Endpoint{
    // TODO

    /**
     * GET /location/navigation/:driver?passengerUid=
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the navigation from the current driver’s road to the passenger’s
     * road with minimal time.
     */
    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        // make true the input is well-formatted
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
            // parse the url to get proper params
            final String driverUid = r.getRequestURI().getPath().split("/")[3];
            final String passengerUid = r.getRequestURI().getQuery().split("=")[1];
            // retrieve driver's road by driverUid as from and passenger's road by passengerUid as to
            String from,to;
            Result result = this.dao.getUserLocationByUid(driverUid);
            if (result.hasNext()) {
                Record user = result.next();
                from = user.get("n.street").asString();
            } else {
                this.sendStatus(r, 404, true);
                return;
            }
            result = this.dao.getUserLocationByUid(passengerUid);
            if (result.hasNext()) {
                Record user = result.next();
                to = user.get("n.street").asString();
            } else {
                this.sendStatus(r, 404, true);
                return;
            }
            // initialize data and keys inside data
            JSONObject data = new JSONObject();
            int totalTime;
            JSONArray route;
            // execute query to get navigation and also the min time
            result = this.dao.getNavigation(from, to);
            if (result.hasNext()) {
                Record nav = result.next();
                totalTime = nav.get("travel_time").asInt();
                route = new JSONArray();
                JSONObject jsonNode = new JSONObject();
                String street = nav.get("nodes(p)").get(0).get("name").asString();
                int time = 0;
                Boolean is_traffic = nav.get("nodes(p)").get(0).get("is_traffic").asBoolean();
                jsonNode.put("street", street);
                jsonNode.put("time", time);
                jsonNode.put("is_traffic", is_traffic);
                route.put(jsonNode);

                for(int i=1; i<nav.get("nodes(p)").size(); i++) {
                    jsonNode = new JSONObject();
                    street = nav.get("nodes(p)").get(i).get("name").asString();
                    time = nav.get("relationships(p)").get(i-1).get("travel_time").asInt();
                    is_traffic = nav.get("nodes(p)").get(i).get("is_traffic").asBoolean();
                    jsonNode.put("street", street);
                    jsonNode.put("time", time);
                    jsonNode.put("is_traffic", is_traffic);
                    route.put(jsonNode);
//                    JSONObject jsonNode = new JSONObject();
//                    String street = nav.get("nodes(p)").get(i).get("name").asString();
//                    int time = nav.get("p").get(3*i-2).get("travel_time").asInt();
//                    Boolean is_traffic = nav.get("nodes(p)").get(i).get("is_traffic").asBoolean();
//                    jsonNode.put("street", street);
//                    jsonNode.put("time", time);
//                    jsonNode.put("is_traffic", is_traffic);
//                    route.put(jsonNode);
                }
            } else {
                this.sendStatus(r, 404, true);
                return;
            }
            data.put("total_time", totalTime);
            data.put("route", route);
            JSONObject res = new JSONObject();
            res.put("status", "OK");
            res.put("data", data);
            this.sendResponse(r, res, 200);
            return;
        } catch (Exception e){
            e.printStackTrace();
            this.sendStatus(r, 500, true);
            return;
        }
    }
}
