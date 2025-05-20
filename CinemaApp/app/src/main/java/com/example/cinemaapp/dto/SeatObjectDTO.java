package com.example.cinemaapp.dto;

import java.io.Serializable;
import java.util.List;

public class SeatObjectDTO implements Serializable {
    private List<SeatDetailDTO> seatDetailDTOList;

    public SeatObjectDTO() {
    }

    public SeatObjectDTO(List<SeatDetailDTO> seatDetailDTOList) {
        this.seatDetailDTOList = seatDetailDTOList;
    }

    public List<SeatDetailDTO> getSeatDetailDTOList() {
        return seatDetailDTOList;
    }

    public void setSeatDetailDTOList(List<SeatDetailDTO> seatDetailDTOList) {
        this.seatDetailDTOList = seatDetailDTOList;
    }
}
