package com.example.minhajlib.elaaj1;

/**
 * Created by Minhaj lib on 5/8/2017.
 */

public class DbModalOrg {

    private String orgId,orgName,orgNumber,orgRegistrationId,description,orgEmail,orgFb,orgWeb,orgType,orgCity,orgLogoUrl;
    private Boolean isHide, isVerified,isLinkToOrg;

    public DbModalOrg(){
        //constructor for firebase
    }

    public DbModalOrg(String orgId,String orgName,String orgNumber,String orgRegistrationId,String description,String orgEmail,String orgFb,String orgWeb,String orgType,String orgCity,
                      String orgLogoUrl,Boolean isHide,Boolean isLinkToOrg,Boolean isVerified){
        setOrgId(orgId);
        setOrgName(orgName);
        setOrgNumber(orgNumber);
        setOrgRegistrationId(orgRegistrationId);
        setDescription(description);
        setOrgEmail(orgEmail);
        setOrgFb(orgFb);
        setOrgWeb(orgWeb);
        setOrgType(orgType);
        setOrgCity(orgCity);
        setOrgLogoUrl(orgLogoUrl);
        setHide(isHide);
        setLinkToOrg(isLinkToOrg);
        setVerified(isVerified);
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgNumber() {
        return orgNumber;
    }

    public void setOrgNumber(String orgNumber) {
        this.orgNumber = orgNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrgEmail() {
        return orgEmail;
    }

    public void setOrgEmail(String orgEmail) {
        this.orgEmail = orgEmail;
    }

    public String getOrgFb() {
        return orgFb;
    }

    public void setOrgFb(String orgFb) {
        this.orgFb = orgFb;
    }

    public String getOrgWeb() {
        return orgWeb;
    }

    public void setOrgWeb(String orgWeb) {
        this.orgWeb = orgWeb;
    }

    public String getOrgType() {
        return orgType;
    }

    public void setOrgType(String orgYpe) {
        this.orgType = orgYpe;
    }

    public String getOrgCity() {
        return orgCity;
    }

    public void setOrgCity(String orgCity) {
        this.orgCity = orgCity;
    }

    public String getOrgLogoUrl() {
        return orgLogoUrl;
    }

    public void setOrgLogoUrl(String orgLogoUrl) {
        this.orgLogoUrl = orgLogoUrl;
    }

    public Boolean getHide() {
        return isHide;
    }

    public void setHide(Boolean hide) {
        isHide = hide;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public String getOrgRegistrationId() {
        return orgRegistrationId;
    }

    public void setOrgRegistrationId(String orgRegistrationId) {
        this.orgRegistrationId = orgRegistrationId;
    }

    public Boolean getLinkToOrg() {
        return isLinkToOrg;
    }

    public void setLinkToOrg(Boolean linkToOrg) {
        isLinkToOrg = linkToOrg;
    }
}
