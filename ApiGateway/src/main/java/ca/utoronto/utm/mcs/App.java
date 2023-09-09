package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
   static int PORT = 8000;

   public static void main(String[] args) throws IOException {
      HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
      server.createContext("/location", new LocationRequest());
      server.createContext("/trip", new TripRequest());
      server.createContext("/user", new UserRequest());
      server.start();

      System.out.printf("Server started on port %d...\n", PORT);
   }
}
