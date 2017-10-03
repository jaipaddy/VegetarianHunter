package com.jpadmanabhan.vegetarianhunter;

/**
 * Created by jpadmanabhan on 4/4/2015.
 */
class Restaurant {
    private String name = "";
    private String description= "";
    private String distance= "";
    private String address= "";
    private String city= "";
    private String phone= "";
    private String rating= "";

    public String toString(){
        return name + "\n"+ distance+" mi\n"+ description+ "\n"+ phone + "\n"+ address+ " " +city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getPhone() {
        return phone;
    }

    public String getRating() {
        return rating;
    }
}
