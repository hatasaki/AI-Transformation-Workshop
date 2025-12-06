package com.example.hotelsearch.controller;

import com.example.hotelsearch.model.Hotel;
import com.example.hotelsearch.model.HotelSearchRequest;
import com.example.hotelsearch.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping("/search")
    public List<Hotel> search(@Valid @RequestBody HotelSearchRequest request) {
        return hotelService.search(request.getCity(), request.getMaxPrice(), request.getFeature());
    }
}
