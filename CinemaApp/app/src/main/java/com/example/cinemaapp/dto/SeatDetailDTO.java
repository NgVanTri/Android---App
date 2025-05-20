package com.example.cinemaapp.dto;

import java.io.Serializable;

public class SeatDetailDTO implements Serializable {
    private Integer id;
    private String status;
    private Integer seatId;
    private String seatName;
    private Integer userId;

    public SeatDetailDTO(Integer id, String status, Integer seatId, String seatName, Integer userId) {
        this.id = id;
        this.status = status;
        this.seatId = seatId;
        this.seatName = seatName;
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSeatId() {
        return seatId;
    }

    public void setSeatId(Integer seatId) {
        this.seatId = seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
