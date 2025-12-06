package com.example.travelrequest.controller;

import com.example.travelrequest.model.TravelRequest;
import com.example.travelrequest.service.TravelRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/travel-requests")
public class TravelRequestApiController {

    private final TravelRequestService travelRequestService;

    public TravelRequestApiController(TravelRequestService travelRequestService) {
        this.travelRequestService = travelRequestService;
    }

    @GetMapping
    public List<TravelRequest> list() {
        return travelRequestService.findAll();
    }

    @PostMapping
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public TravelRequest create(@Valid @RequestBody TravelRequest request) {
        return travelRequestService.create(request);
    }
}
