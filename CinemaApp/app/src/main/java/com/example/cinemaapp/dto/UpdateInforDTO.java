package com.example.cinemaapp.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


public class UpdateInforDTO implements Serializable {
    private String email;
    private String fullName;
    private String telephone;
    private String birthday;
    private String gender;
    private String province;
    private String address;

    public UpdateInforDTO() {
    }

    public UpdateInforDTO(String email, String fullName, String telephone, String birthday, String gender, String province, String address) {
        this.email = email;
        this.fullName = fullName;
        this.telephone = telephone;
        this.birthday = birthday;
        this.gender = gender;
        this.province = province;
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
