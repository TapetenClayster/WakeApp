package org.ssc;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.ssc.model.Location;

import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public final class WakeNavigation {
    private static final Gson gson = new Gson();
    private static final String API_KEY = "5b3ce3597851110001cf6248348722fe73aa4a40bd18d4bb9afff3eb";
    private static final String BASE_URL = "https://api.openrouteservice.org";
    private static final String BASE_SEARCH_URL = String.format("%s/geocode/search", BASE_URL);
    private static final String BASE_NAVIGATION_URL = String.format("%s/v2/directions", BASE_URL);
    private static final String BASE_URL_BVG = "https://v5.bvg.transport.rest";
    private static final String BASE_URL_BVG_LOCATION = String.format("%s/locations", BASE_URL_BVG);
    private static final String BASE_URL_BVG_NAVIGATION = String.format("%s/journeys", BASE_URL_BVG);

    private static final Map<Integer, String> transportTypeMap = Map.of(
            1, "driving-car",
            2, "cycling-regular",
            3, "bvg",
            4, "foot-walking",
            5, "wheelchair"
    );

    public static List<Location> searchLocationRequest(String searchInput) {
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response;

        try {
            response = client.send(
                    createSearchRequest(searchInput),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sanitizeSearchBody(response);
    }

    private static HttpRequest createSearchRequest(String searchInput) {
        RequestURIBuilder builder = new RequestURIBuilder(BASE_SEARCH_URL);

        String sanitizedSearchInput = String.format("%s %s", searchInput, "Berlin").replace(" ", "%20");

        try {
            builder.addParameter("api_key", API_KEY);
            builder.addParameter("size", 3); //Wir brauchen nur 3 potentielle Koordinaten
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
        Type jsonElement = new TypeToken<JsonElement>() {
        }.getType();
        JsonElement result = gson.fromJson(response.body(), jsonElement);

        List<Location> entries = new ArrayList<>();

        JsonArray addresses = result.getAsJsonObject().get("features").getAsJsonArray();

        for (JsonElement el : addresses) {
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

    public static Duration navigationRequest(Location startLocation, Location endLocation, int transportMethod) {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response;

        try {
            response = client.send(
                    createNavigationRequest(startLocation, endLocation, transportTypeMap.get(transportMethod)),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (transportMethod == 3)
            return sanitizeBvgNavigationBody(response);
        else
            return sanitizeNavigationBody(response);
    }

    private static HttpRequest createNavigationRequest(Location startLocation, Location destinationLocation, String transportMethod) {
        RequestURIBuilder builder;
        if (!transportMethod.equals("bvg")) {
            builder = new RequestURIBuilder(String.format("%s/%s", BASE_NAVIGATION_URL, transportMethod));

            try {
                builder.addParameter("api_key", API_KEY);
                builder.addParameter("start", String.format("%s,%s", startLocation.longitude, startLocation.latitude));
                builder.addParameter("end", String.format("%s,%s", destinationLocation.longitude, destinationLocation.latitude));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            builder = new RequestURIBuilder(BASE_URL_BVG_NAVIGATION);

            Location startLocationBvg = searchBvgLocations(startLocation);
            Location destinationlocationBvg = searchBvgLocations(destinationLocation);

            try {
                assert startLocationBvg != null && destinationlocationBvg != null;

                if (startLocationBvg.bvgId != null)
                    builder.addParameter("from", startLocationBvg.bvgId);
                else
                    builder.addParameter("from.address", startLocationBvg.name);
                builder.addParameter("from.longitude", startLocationBvg.longitude);
                builder.addParameter("from.latitude", startLocationBvg.latitude);

                if (destinationlocationBvg.bvgId != null)
                    builder.addParameter("to", destinationlocationBvg.bvgId);
                else
                    builder.addParameter("to.address", destinationlocationBvg.name);
                builder.addParameter("to.longitude", destinationlocationBvg.longitude);
                builder.addParameter("to.latitude", destinationlocationBvg.latitude);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return HttpRequest.newBuilder()
                .uri(builder.toURI())
                .GET()
                .build();
    }

    private static Location searchBvgLocations(Location searchLocation) {
        RequestURIBuilder builder = new RequestURIBuilder(BASE_URL_BVG_LOCATION);
        HttpResponse<String> response;

        try {
            builder.addParameter("results", 1);
            String searchQuery = !searchLocation.street.isEmpty() ? searchLocation.street : searchLocation.name;
            builder.addParameter("query", searchQuery);

            response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder().uri(builder.toURI()).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Type jsonElement = new TypeToken<JsonElement>() {
        }.getType();
        JsonElement responseBody = gson.fromJson(response.body(), jsonElement);

        JsonObject result = responseBody.getAsJsonArray()
                .get(0).getAsJsonObject();

        JsonObject resultLocation;

        //Notwendig, weil es kein Uniformes output Format gibt
        if (result.has("location"))
            resultLocation = result.get("location").getAsJsonObject();
        else
            resultLocation = result;

        return new Location(
                result.has("name") ? result.get("name").getAsString() : result.get("address").getAsString(),
                resultLocation.get("longitude").getAsString(),
                resultLocation.get("latitude").getAsString(),
                result.get("id").isJsonNull() ? null : result.get("id").getAsString()
        );
    }

    private static Duration sanitizeNavigationBody(HttpResponse<String> response) throws NoSuchElementException {
        Type jsonElement = new TypeToken<JsonElement>() {
        }.getType();
        JsonElement responseBody = gson.fromJson(response.body(), jsonElement);

        try {
            double totalTravelTime = responseBody.getAsJsonObject()
                    .get("features").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("properties").getAsJsonObject()
                    .get("segments").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("duration").getAsDouble();

            return Duration.ofSeconds(Double.valueOf(totalTravelTime).longValue());
        } catch (Exception e) {
            throw new NoSuchElementException("Travel duration was not found");
        }
    }

    private static Duration sanitizeBvgNavigationBody(HttpResponse<String> response) throws NoSuchElementException {
        Type jsonElement = new TypeToken<JsonElement>() {
        }.getType();
        JsonElement responseBody = gson.fromJson(response.body(), jsonElement);

        try {
            JsonArray currNavigation = responseBody.getAsJsonObject()
                    .get("journeys").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("legs").getAsJsonArray();

            ZonedDateTime startTime = ZonedDateTime.parse(currNavigation.get(0).getAsJsonObject()
                    .get("plannedDeparture").getAsString()
            );
            ZonedDateTime arrivalTime = ZonedDateTime.parse(currNavigation.get(currNavigation.size() - 1).getAsJsonObject()
                    .get("plannedArrival").getAsString()
            );

            return Duration.between(startTime, arrivalTime);
        } catch (Exception e) {
            throw new NoSuchElementException("No Start or Arrival time were found in the response body");
        }
    }

    private static String getStringDataFromJsonObject(JsonObject obj, String key) {
        if (obj.has(key)) {
            return obj.get(key).getAsString();
        }
        return "";
    }
}
