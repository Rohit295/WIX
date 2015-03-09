package com.drr.wix.model;

/**
 * Created by racastur on 02-03-2015.
 */
public class OrganizationDTO {

    private Long id;

    private String name;

    private String orgType;

    public OrganizationDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgType) {
        this.orgType = orgType;
    }

}
