package com.drr.wix.model;

/**
 * Created by racastur on 12-11-2014.
 */
public class LocationDTO {

    private Double latitude;

    private Double longitude;

    private String address;

    public LocationDTO() {

    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
