package com.drr.wix.model;

/**
 * Created by racastur on 12-11-2014.
 */
public class RouteExecutionLocationDTO {

    private Long id;

    private LocationDTO location;

    private Long timestamp;    // utc time

    private Long deviceId;

    public RouteExecutionLocationDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

}
