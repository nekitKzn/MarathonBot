package com.nekitvp.marathonbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final UserService userService;
    private final MarathonService marathonService;

    public void resetCountToZero() {
        userService.resetCount();
    }

    public void sendResultReport() {
        marathonService.sendStatistics();
    }
}
