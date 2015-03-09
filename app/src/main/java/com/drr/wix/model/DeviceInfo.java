package com.drr.wix.model;

/**
 * Created by racastur on 01-11-2014.
 */
public class DeviceInfo {

    private Long id;

    private String gcmRegistrationId;

    public DeviceInfo() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGcmRegistrationId() {
        return gcmRegistrationId;
    }

    public void setGcmRegistrationId(String gcmRegistrationId) {
        this.gcmRegistrationId = gcmRegistrationId;
    }
}
