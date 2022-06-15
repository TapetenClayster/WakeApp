package org.ssc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.ssc.model.Location;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.util.*;

public final class WakeNavigation {
    private static final Gson gson = new Gson();
    private static final String API_KEY = "";
    private static final String BASE_URL = "https://api.openrouteservice.org";
    private static final String BASE_SEARCH_URL = BASE_URL + "/geocode/search";
    private static final String BASE_NAVIGATION_URL = BASE_URL + "/v2/directions";

    private static final Map<String, String> transportMethod = Map.of(
            "Auto", "driving-car",
            "Fahrrad", "cycling-regular",
            "ÖPNV", "bus",
            "Laufen", "foot-walking",
            "Rollstuhl", "wheelchair"
    );

    public static List<Location> searchLocationRequest(String searchInput) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(createSearchRequest(searchInput), HttpResponse.BodyHandlers.ofString());

        return sanitizeSearchBody(response);
    }

    private static HttpRequest createSearchRequest(String searchInput) {
        RequestURIBuilder builder = new RequestURIBuilder(BASE_SEARCH_URL);

        String sanitizedSearchInput = searchInput.replace(" ", "%20");

        try {
            builder.addParameter("api_key", API_KEY);
            builder.addParameter("size", 3); //Wir brauchen nur 3 potentielle Koordinaten
            builder.addParameter("region", "Berlin");
            builder.addParameter("text", sanitizedSearchInput);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return HttpRequest.newBuilder()
                .uri(builder.toURI())
                .GET()
                .build();
    }

    private static List<Location> sanitizeSearchBody(HttpResponse<String> response) {
        Type jsonElement = new TypeToken<JsonElement>() {}.getType();
        JsonElement result = gson.fromJson(response.body(), jsonElement);

        List<Location> entries = new ArrayList<>();

        JsonArray addresses = result.getAsJsonObject().get("features").getAsJsonArray();

        for(JsonElement el : addresses){
            JsonArray coordData = el.getAsJsonObject().get("geometry").getAsJsonObject().get("coordinates").getAsJsonArray();
            JsonObject addressData = el.getAsJsonObject().get("properties").getAsJsonObject();

            entries.add(new Location(
                    getStringDataFromJsonObject(addressData, "name"),
                    getStringDataFromJsonObject(addressData, "street"),
                    getStringDataFromJsonObject(addressData, "housenumber"),
                    getStringDataFromJsonObject(addressData, "postalcode"),
                    getStringDataFromJsonObject(addressData, "region"),
                    getStringDataFromJsonObject(addressData, "country"),
                    coordData.get(0).getAsString(),
                    coordData.get(1).getAsString()
            ));
        }

        return entries;
    }

    public static String navigationRequest(Location startLocation, Location endLocation, String transportMethod) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client.send(createNavigationRequest(startLocation, endLocation, transportMethod), HttpResponse.BodyHandlers.ofString());

        return sanitizeNavigationBody(response);
    }

    private static HttpRequest createNavigationRequest(Location startLocation, Location destinationLocation, String transportMethod){
        RequestURIBuilder builder = new RequestURIBuilder(BASE_NAVIGATION_URL + "/driving-car");
        //TODO: Add transport method handling

        try {
            builder.addParameter("api_key", API_KEY);
            builder.addParameter("region", "Berlin");
            builder.addParameter("start", String.format("%s,%s", startLocation.longitude, startLocation.latitude));
            builder.addParameter("end", String.format("%s,%s", destinationLocation.longitude, destinationLocation.latitude));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return HttpRequest.newBuilder()
                .uri(builder.toURI())
                .GET()
                .build();
    }

    private static String sanitizeNavigationBody(HttpResponse<String> response) throws NoSuchElementException {
        Type jsonElement = new TypeToken<JsonElement>() {}.getType();
        JsonElement responseBody = gson.fromJson(response.body(), jsonElement);

        try {
            double totalTravelTime = responseBody.getAsJsonObject()
                    .get("features").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("properties").getAsJsonObject()
                    .get("segments").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("duration").getAsDouble();

            return Duration.ofSeconds(Double.valueOf(totalTravelTime).longValue()).toString();
        } catch (Exception e){
            throw new NoSuchElementException("Travel duration was not found");
        }
    }

    private static String getStringDataFromJsonObject(JsonObject obj, String key){
        if(obj.has(key)){
            return obj.get(key).getAsString();
        }
        return "";
    }
}
