package ca.utoronto.utm.mcs;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class MongoDAO {
  private MongoCollection<Document> collection;

  public MongoDAO() {
    Dotenv dotenv = Dotenv.load();
    String addr = dotenv.get("MONGODB_ADDR");
    if (addr == null) {
      System.out.println("MongoDB address not found in .env file. Please add it.");
      System.exit(1);
    }

    String url = "mongodb://root:123456@" + addr + ":27017";

    MongoClient client = MongoClients.create(url);
    MongoDatabase db = client.getDatabase("trip");
    this.collection = db.getCollection("trips");
  }

  private JSONObject cleanTrip(JSONObject trip) throws JSONException {
    JSONObject _id_wrapper = trip.getJSONObject("_id");
    String _id = _id_wrapper.getString("$oid");
    trip.put("_id", _id);

    JSONObject startTime_wrapper = trip.getJSONObject("startTime");
    String startTime = startTime_wrapper.getString("$numberLong");
    trip.put("startTime", startTime);

    if (trip.has("endTime")) {
      JSONObject endTime_wrapper = trip.getJSONObject("endTime");
      String endTime = endTime_wrapper.getString("$numberLong");
      trip.put("endTime", endTime);
    }

    if (trip.has("timeElapsed")) {
      JSONObject timeElapsed_wrapper = trip.getJSONObject("timeElapsed");
      String timeElapsed = timeElapsed_wrapper.getString("$numberLong");
      trip.put("timeElapsed", timeElapsed);
    }

    return trip;
  }

  private JSONObject documentToJSON(Document doc) throws JSONException {
    return cleanTrip(new JSONObject(doc.toJson()));
  }

  private JSONArray documentListToJSONArray(FindIterable<Document> docs) throws JSONException {
    JSONArray array = new JSONArray();
    for (Document doc : docs) {
      array.put(documentToJSON(doc));
    }
    return array;
  }

  public JSONObject initTrip(String driver, String passenger, long startTime) throws JSONException {
    Document trip = new Document();
    trip.append("driver", driver);
    trip.append("passenger", passenger);
    trip.append("startTime", startTime);
    this.collection.insertOne(trip);
    return documentToJSON(trip);
  }

  public boolean completeTrip(String _id, int distance, long endTime, long timeElapsed, double discount,
      double totalCost,
      double driverPayout) {
    Document trip = this.collection.find(eq("_id", new ObjectId(_id))).first();
    if (trip == null) {
      return false;
    }

    trip.append("distance", distance);
    trip.append("endTime", endTime);
    trip.append("timeElapsed", timeElapsed);
    trip.append("discount", discount);
    trip.append("totalCost", totalCost);
    trip.append("driverPayout", driverPayout);

    this.collection.replaceOne(eq("_id", new ObjectId(_id)), trip);
    return true;
  }

  public JSONObject getTrip(String _id) throws JSONException {
    Document trip = this.collection.find(eq("_id", new ObjectId(_id))).first();
    if (trip == null) {
      return null;
    }
    return documentToJSON(trip);
  }

  public JSONArray getPassengerTrips(String passenger) throws JSONException {
    return documentListToJSONArray(this.collection.find(eq("passenger", passenger)));
  }

  public JSONArray getDriverTrips(String driver) throws JSONException {
    return documentListToJSONArray(this.collection.find(eq("driver", driver)));
  }
}
