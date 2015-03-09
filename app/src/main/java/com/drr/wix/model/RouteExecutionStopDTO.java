package com.drr.wix.model;

/**
 * Created by racastur on 12-11-2014.
 */
public class RouteExecutionStopDTO {

    private Long routeStopId;

    private Long visitedTime;  // utc time

    public RouteExecutionStopDTO() {

    }

    public Long getRouteStopId() {
        return routeStopId;
    }

    public void setRouteStopId(Long routeStopId) {
        this.routeStopId = routeStopId;
    }

    public Long getVisitedTime() {
        return visitedTime;
    }

    public void setVisitedTime(Long visitedTime) {
        this.visitedTime = visitedTime;
    }

}
