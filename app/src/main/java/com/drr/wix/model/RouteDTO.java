package com.drr.wix.model;

import java.util.List;

/**
 * Created by racastur on 12-11-2014.
 */
public class RouteDTO {

    private Long id;

    private OrganizationDTO organization;

    private String name;

    private String defaultStopPurpose; // should be enum { Delivery, Pickup, Visit }

    private String executionStartTime; // a one time route or a scheduled route

    private List<RouteStopDTO> routeStops;

    private List<RouteLocationDTO> routeLocations;

    public RouteDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultStopPurpose() {
        return defaultStopPurpose;
    }

    public void setDefaultStopPurpose(String defaultStopPurpose) {
        this.defaultStopPurpose = defaultStopPurpose;
    }

    public String getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionStartTime(String executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    public List<RouteStopDTO> getRouteStops() {
        return routeStops;
    }

    public void setRouteStops(List<RouteStopDTO> routeStops) {
        this.routeStops = routeStops;
    }

    public List<RouteLocationDTO> getRouteLocations() {
        return routeLocations;
    }

    public void setRouteLocations(List<RouteLocationDTO> routeLocations) {
        this.routeLocations = routeLocations;
    }

}
