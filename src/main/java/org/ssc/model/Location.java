package org.ssc.model;

public class Location {
    public final String name;
    public final String street;
    public final String housenumber;
    public final String postalcode;
    public final String region;
    public final String country;
    public final String longitude;
    public final String latitude;
    public final String bvgId;

    public Location(String name, String street, String housenumber, String postalcode, String region, String country, String longitude, String latitude) {
        this.name = name;
        this.street = street;
        this.housenumber = housenumber;
        this.postalcode = postalcode;
        this.region = region;
        this.country = country;
        this.longitude = longitude;
        this.latitude = latitude;
        this.bvgId = null;
    }

    public Location(String name, String longitude, String latitude, String bvgId){
        this.name = name;
        this.street = null;
        this.housenumber = null;
        this.postalcode = null;
        this.region = null;
        this.country = null;
        this.longitude = longitude;
        this.latitude = latitude;
        this.bvgId = bvgId;
    }

    public String toString() {
        String output = "";

        if (!this.name.isEmpty()){
            output += this.name + "\n";
        }
        if(this.street != null && !this.street.isEmpty() && !this.street.equals(this.name)){
            if(this.housenumber != null && this.housenumber.isEmpty())
                output += this.street + "\n";
            else
                output += String.format("%s, %s\n", this.street, this.housenumber);
        }
        if(this.postalcode != null && !this.postalcode.isEmpty()){
            output += String.format("%s, %s\n", this.postalcode, this.region);
        } else {
            output += String.format("%s\n", this.region);
        }

        output += String.format("%s\n", this.country);

        return output;
    }

}
