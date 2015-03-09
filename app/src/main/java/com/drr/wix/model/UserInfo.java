package com.drr.wix.model;

/**
 * Created by racastur on 01-11-2014.
 */
public class UserInfo {

    private Long id;

    private String emailId;

    public UserInfo() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
