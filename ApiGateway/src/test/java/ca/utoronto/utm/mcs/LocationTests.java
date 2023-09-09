package ca.utoronto.utm.mcs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
/*
Please write your tests for the Location Microservice in this class. 
*/

public class LocationTests {
    private final static String API_URL = "http://localhost:8004/";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody)).build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    public static void setUp() throws JSONException, IOException, InterruptedException {
        // set up for [GET /location/nearbyDriver/:uid?radius=]
        JSONObject confirmReq = new JSONObject().put("uid", "1").put("is_driver", false);
        sendRequest("/location/user", "PUT", confirmReq.toString());
        confirmReq = new JSONObject().put("uid", "2").put("is_driver", true);
        sendRequest("/location/user", "PUT", confirmReq.toString());

        //set up for [GET /location/navigation/:driver?passengerUid=]
        confirmReq = new JSONObject().put("longitude", 2.0).put("latitude", 2.0).put("street","Black");
        sendRequest("/location/1", "PATCH", confirmReq.toString());
        confirmReq = new JSONObject().put("longitude", 3.0).put("latitude", 3.0).put("street","Red");
        sendRequest("/location/2", "PATCH", confirmReq.toString());
        confirmReq = new JSONObject().put("roadName", "Black").put("hasTraffic", true);
        sendRequest("/location/road", "PUT", confirmReq.toString());
        confirmReq = new JSONObject().put("roadName", "Red").put("hasTraffic", true);
        sendRequest("/location/road", "PUT", confirmReq.toString());
        confirmReq = new JSONObject().put("roadName1", "Red").put("roadName2", "Black").
                put("hasTraffic",true).put("time", 35);
        sendRequest("/location/hasRoute", "POST", confirmReq.toString());
    }

    @Test
    public void getNearbyDriverPass() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/location/nearbyDriver/1?radius=2", "GET", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void getNearbyDriverFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/location/nearbyDriver/", "GET", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void getNavigationPass() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/location/navigation/2?passengerUid=1", "GET", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void getNavigationFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/location/navigation/", "GET", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }
}
