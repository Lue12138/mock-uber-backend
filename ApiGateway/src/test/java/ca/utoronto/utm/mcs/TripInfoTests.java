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
Please write your tests for the TripInfo Microservice in this class. 
*/

public class TripInfoTests {
    private final static String API_URL = "http://localhost:8004/";
    private String tripID = "";

    private static HttpResponse<String> sendRequest(String endpoint, String method, String reqBody)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + endpoint))
                .method(method, HttpRequest.BodyPublishers.ofString(reqBody)).build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    public void setUp() throws JSONException, IOException, InterruptedException {
        // set up for [PATCH /trip/:_id], [GET /trip/passenger/:uid], [GET /trip/driver/:uid], [GET /trip/driverTime/:_id]
        JSONObject confirmReq = new JSONObject().put("driver", "106").put("passenger", "105" ).put("startTime", 9);
        sendRequest("/trip/confirm", "POST", confirmReq.toString());
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        this.tripID = new JSONObject(confirmRes.body()).getJSONObject("data").getString("_id");
    }

    @Test
    public void tripRequestPass() throws IOException, InterruptedException, JSONException {
        JSONObject confirmReq = new JSONObject().put("uid", "101").put("radius", 5);
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void tripRequestFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/request", "POST", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void tripConfirmPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject().put("driver", "102").put("passenger", "103").put("startTime",9);
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void tripConfirmFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/confirm", "POST", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void patchTripPass() throws JSONException, IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject().put("distance", 12).put("endTime", 9)
                .put("timeElapsed",2).put("discount", 0).put("totalCost", 100.0).put("driverPayout", 65.0);
        HttpResponse<String> confirmRes = sendRequest("/trip/"+tripID, "PATCH", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void patchTripFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/104", "PATCH", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void tripsForPassengerPass() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/passenger/105", "GET", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void tripsForPassengerFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/passenger/", "GET", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void tripsForDriverPass() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/driver/106", "GET", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void tripsForDriverFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/driver/", "GET", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void driverTimePass() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/driverTime/"+tripID, "GET", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void driverTimeFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/trip/driverTime/", "GET", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }
}