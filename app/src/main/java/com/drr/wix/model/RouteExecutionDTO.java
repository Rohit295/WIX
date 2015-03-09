package com.drr.wix.model;

import com.drr.wix.model.RouteExecutionLocationDTO;
import com.drr.wix.model.RouteExecutionStopDTO;

import java.util.List;

/**
 * Created by racastur on 12-11-2014.
 */
public class RouteExecutionDTO {

    private Long id;

    private Long routeId;

    private List<RouteExecutionStopDTO> routeExecutionStops;

    private List<RouteExecutionLocationDTO> routeExecutionLocations;

    private Long startTime;  // utc time

    private Long endTime;    // utc time

    private Long routeExecutorId;

    public RouteExecutionDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public List<RouteExecutionStopDTO> getRouteExecutionStops() {
        return routeExecutionStops;
    }

    public void setRouteExecutionStops(List<RouteExecutionStopDTO> routeExecutionStops) {
        this.routeExecutionStops = routeExecutionStops;
    }

    public List<RouteExecutionLocationDTO> getRouteExecutionLocations() {
        return routeExecutionLocations;
    }

    public void setRouteExecutionLocations(List<RouteExecutionLocationDTO> routeExecutionLocations) {
        this.routeExecutionLocations = routeExecutionLocations;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getRouteExecutorId() {
        return routeExecutorId;
    }

    public void setRouteExecutorId(Long routeExecutorId) {
        this.routeExecutorId = routeExecutorId;
    }

}
