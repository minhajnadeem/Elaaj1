package com.example.minhajlib.elaaj1;

import android.net.Uri;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Minhaj lib on 4/4/2017.
 */

public class DbModal implements Serializable {

    private String bloodGroup;
    private String city;
    private String address;
    private String contact;
    private String isDonor;
    private String name;
    private String id;
    private String photoUrl;
    private String lastDonated;
    private String association;
    private String orgName;
    private String timeAvailable;
    private String bloodPrice;
    private Boolean hasOrg;
    private Boolean isPaid;



    public DbModal() {
        // required empty constructor
    }

    public DbModal(String id, String photoUrl, String name, String bloodGroup, String city, String contact, String address,
                   String isDonor, String association, String lastDonated, String orgName, String timeAvailable
            , Boolean isPaid, String bloodPrice) {

        setBloodGroup(bloodGroup);
        setCity(city);
        setContact(contact);
        setAddress(address);
        setIsDonor(isDonor);
        setName(name);
        setId(id);
        setPhotoUrl(photoUrl);
        setAssociation(association);
        setLastDonated(lastDonated);
        setOrgName(orgName);
        setTimeAvailable(timeAvailable);
        setPaid(isPaid);
        setBloodPrice(bloodPrice);
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIsDonor() {
        return isDonor;
    }

    public void setIsDonor(String isDonor) {
        this.isDonor = isDonor;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getLastDonated() {
        return lastDonated;
    }

    public void setLastDonated(String lastDonated) {
        this.lastDonated = lastDonated;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Boolean getHasOrg() {
        return hasOrg;
    }

    public void setHasOrg(Boolean hasOrg) {
        this.hasOrg = hasOrg;
    }

    public String getTimeAvailable() {
        return timeAvailable;
    }

    public void setTimeAvailable(String timeAvailable) {
        this.timeAvailable = timeAvailable;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public String getBloodPrice() {
        return bloodPrice;
    }

    public void setBloodPrice(String bloodPrice) {
        this.bloodPrice = bloodPrice;
    }
}
