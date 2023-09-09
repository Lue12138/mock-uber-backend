package ca.utoronto.utm.mcs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

public abstract class Endpoint implements HttpHandler {
  private String target;
  public HashMap<Integer, String> errorMap;

  public Endpoint(String target) {
    this.target = target;
    errorMap = new HashMap<>();
    errorMap.put(200, "OK");
    errorMap.put(400, "BAD REQUEST");
    errorMap.put(403, "FORBIDDEN");
    errorMap.put(404, "NOT FOUND");
    errorMap.put(405, "METHOD NOT ALLOWED");
    errorMap.put(500, "INTERNAL SERVER ERROR");
  }

  public void handle(HttpExchange r) {
    try {
      switch (r.getRequestMethod()) {
        case "GET":
          this.handleGet(r);
          break;
        case "PATCH":
          this.handlePatch(r);
          break;
        case "POST":
          this.handlePost(r);
          break;
        case "PUT":
          this.handlePut(r);
          break;
        case "DELETE":
          this.handleDelete(r);
          break;
        default:
          this.sendStatus(r, 405);
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void writeOutputStream(HttpExchange r, String response) throws IOException {
    OutputStream os = r.getResponseBody();
    os.write(response.getBytes());
    os.close();
  }

  public void sendResponse(HttpExchange r, JSONObject obj, int statusCode) throws JSONException, IOException {
    obj.put("status", errorMap.get(statusCode));
    String response = obj.toString();
    r.sendResponseHeaders(statusCode, response.length());
    this.writeOutputStream(r, response);
  }

  public void sendStatus(HttpExchange r, int statusCode) throws JSONException, IOException {
    JSONObject res = new JSONObject();
    res.put("status", errorMap.get(statusCode));
    String response = res.toString();
    r.sendResponseHeaders(statusCode, response.length());
    this.writeOutputStream(r, response);
  }

  public void sendStatus(HttpExchange r, int statusCode, boolean hasEmptyData) throws JSONException, IOException {
    JSONObject res = new JSONObject();
    res.put("status", errorMap.get(statusCode));
    res.put("data", new JSONObject());
    String response = res.toString();
    r.sendResponseHeaders(statusCode, response.length());
    this.writeOutputStream(r, response);
  }

  private void dispatchRequest(HttpExchange r, String method) throws JSONException, IOException {
    String uri = "http://" + target + ":8000" + r.getRequestURI().getPath();
    String query = r.getRequestURI().getQuery();
    if (query != null) {
      uri += "?" + query;
    }

    int bodyLen = r.getRequestBody().available();
    String reqBody = "";
    if (bodyLen > 0) {
      reqBody = new JSONObject(Utils.convert(r.getRequestBody())).toString();
    }

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri))
        .method(method, HttpRequest.BodyPublishers.ofString(reqBody)).build();

    try {
      HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
      this.sendResponse(r, new JSONObject(response.body()), response.statusCode());
    } catch (InterruptedException e) {
      this.sendStatus(r, 500);
    }
  }

  public void handleGet(HttpExchange r) throws IOException, JSONException {
    this.dispatchRequest(r, "GET");
  };

  public void handlePatch(HttpExchange r) throws IOException, JSONException {
    this.dispatchRequest(r, "PATCH");
  };

  public void handlePost(HttpExchange r) throws IOException, JSONException {
    this.dispatchRequest(r, "POST");
  };

  public void handlePut(HttpExchange r) throws IOException, JSONException {
    this.dispatchRequest(r, "PUT");
  };

  public void handleDelete(HttpExchange r) throws IOException, JSONException {
    this.dispatchRequest(r, "DELETE");
  };
}
