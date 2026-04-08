package com.campusbloom.backend.controller;

import com.campusbloom.backend.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @GetMapping("/test")
    public String test() {
        return testService.getTestMessage().message();
    }
}
