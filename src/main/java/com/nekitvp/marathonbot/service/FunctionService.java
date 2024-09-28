package com.nekitvp.marathonbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final UserService userService;

    public void resetCountToZero() {
        userService.resetCount();
    }
}
