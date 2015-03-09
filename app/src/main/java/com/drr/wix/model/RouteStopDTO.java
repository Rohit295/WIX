package com.drr.wix.model;

/**
 * Created by racastur on 12-11-2014.
 */
public class RouteStopDTO {

    private String name;

    private String address; // This should be a class ideally

    private LocationDTO location;

    public RouteStopDTO() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

}
