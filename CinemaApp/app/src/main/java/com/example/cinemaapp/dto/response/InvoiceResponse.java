package com.example.cinemaapp.dto.response;

import java.io.Serializable;
import java.util.List;

public class InvoiceResponse implements Serializable {
    private Integer countTicket;
    private List<String> seatName;
    private Float ticketPrice;
    private Float foodPrice;
    private Float totalPrice;

    public InvoiceResponse(Integer countTicket, List<String> seatName, Float ticketPrice, Float foodPrice, Float totalPrice) {
        this.countTicket = countTicket;
        this.seatName = seatName;
        this.ticketPrice = ticketPrice;
        this.foodPrice = foodPrice;
        this.totalPrice = totalPrice;
    }

    public Integer getCountTicket() {
        return countTicket;
    }

    public void setCountTicket(Integer countTicket) {
        this.countTicket = countTicket;
    }

    public List<String> getSeatName() {
        return seatName;
    }

    public void setSeatName(List<String> seatName) {
        this.seatName = seatName;
    }

    public Float getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Float ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Float getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Float foodPrice) {
        this.foodPrice = foodPrice;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "InvoiceResponse{" +
                "countTicket=" + countTicket +
                ", seatName=" + seatName +
                ", ticketPrice=" + ticketPrice +
                ", foodPrice=" + foodPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
