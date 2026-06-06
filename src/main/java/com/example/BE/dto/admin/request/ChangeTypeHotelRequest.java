package com.example.BE.dto.admin.request;

import com.example.BE.enums.HotelType;

public record   ChangeTypeHotelRequest(
        HotelType hotelType
) {

}
