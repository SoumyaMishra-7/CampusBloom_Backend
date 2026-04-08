package com.campusbloom.backend.service;

import com.campusbloom.backend.model.TestMessage;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    public TestMessage getTestMessage() {
        return new TestMessage("Backend working");
    }
}
