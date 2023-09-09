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
Please write your tests for the User Microservice in this class. 
*/

public class UserTests {
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
        JSONObject confirmReq = new JSONObject().put("name", "James").put("email", "james@gmail.com").put("password","142857");
        sendRequest("/user/register", "POST", confirmReq.toString());
    }

    @Test
    public void userRegisterPass() throws IOException, InterruptedException, JSONException {
        JSONObject confirmReq = new JSONObject().put("name", "Kevin").put("email", "kevin@gmail.com").put("password","123456");
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void userRegisterFail() throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/user/register", "POST", confirmReq.toString());
        assertEquals(400, confirmRes.statusCode());
    }

    @Test
    public void userLoginPass() throws IOException, InterruptedException, JSONException {
        JSONObject confirmReq = new JSONObject().put("email", "james@gmail.com").put("password", "142857");
        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", confirmReq.toString());
        assertEquals(HttpURLConnection.HTTP_OK, confirmRes.statusCode());
    }

    @Test
    public void userLoginFail () throws IOException, InterruptedException {
        JSONObject confirmReq = new JSONObject();
        HttpResponse<String> confirmRes = sendRequest("/user/login", "POST", confirmReq.toString());
        assertEquals(404, confirmRes.statusCode());
    }
}
