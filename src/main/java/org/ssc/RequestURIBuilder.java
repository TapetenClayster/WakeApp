package org.ssc;

import java.net.URI;
import java.text.ParseException;
import java.util.*;

public class RequestURIBuilder {
    private final String baseURL;
    private final Map<String, List<String>> requestParameters;

    public RequestURIBuilder(String baseURL) {
        this.baseURL = baseURL;
        this.requestParameters = new HashMap<>();
    }

    public RequestURIBuilder addParameter(String parameterName, Object... parameterValues) throws ParseException {
        if (parameterValues.length == 0) return this;

        try {
            List<String> values = new ArrayList<>();

            for (Object paramObject : parameterValues) {
                values.add(paramObject.toString());
            }
            this.requestParameters.put(parameterName, values);

            return this;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ParseException("Parameter conversion to string failed", 0);
        }
    }

    public void removeParameter(String parameterName) {
        requestParameters.remove(parameterName);
    }

    public URI toURI() {
        if (requestParameters.size() == 0) {
            return URI.create(baseURL);
        }

        StringBuilder queryString = new StringBuilder(this.baseURL);
        queryString.append("?");

        int pairCounter = 0;
        for (Map.Entry<String, List<String>> parameterPair : requestParameters.entrySet()) {
            pairCounter++;

            queryString.append(parameterPair.getKey()).append("=");

            int valueCounter = 0;
            for (String parameterValue : parameterPair.getValue()) {
                valueCounter++;

                if (parameterValue.contains(" "))
                    parameterValue = parameterValue.replace(" ", "%20");

                queryString.append(parameterValue);
                if (valueCounter != parameterPair.getValue().size()) {
                    queryString.append(",");
                }
            }

            if (pairCounter != requestParameters.size()) {
                queryString.append("&");
            }
        }

        return URI.create(queryString.toString());
    }
}