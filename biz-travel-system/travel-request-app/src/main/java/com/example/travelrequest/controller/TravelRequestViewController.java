package com.example.travelrequest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TravelRequestViewController {

    @GetMapping({"/", "/travel-requests"})
    public String index() {
        return "travel-requests";
    }
}
