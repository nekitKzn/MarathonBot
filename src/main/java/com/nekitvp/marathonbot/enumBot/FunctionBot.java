package com.nekitvp.marathonbot.enumBot;

import com.nekitvp.marathonbot.service.FunctionService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.function.Consumer;

import static com.nekitvp.marathonbot.enumBot.StateBot.ADMIN_LIST_USERS_UPDATE_COUNT;
import static com.nekitvp.marathonbot.enumBot.StateBot.START;

@Getter
@RequiredArgsConstructor
public enum FunctionBot {

    RESET_COUNT(FunctionService::resetCountToZero, ADMIN_LIST_USERS_UPDATE_COUNT, "Обнулить count"),
    SEND_RESULT_REPORT(FunctionService::sendResultReport, START, "Отправить статистику по отчетам");

    private final Consumer<FunctionService> function;
    private final StateBot stateBot;
    private final String buttonForThisFunction;

    public static FunctionBot getFunctionBotByCallBackQuery(String query) {
        return Arrays.stream(values())
                .filter(state -> query.equals(state.name()))
                .findFirst()
                .orElse(null);
    }
}
