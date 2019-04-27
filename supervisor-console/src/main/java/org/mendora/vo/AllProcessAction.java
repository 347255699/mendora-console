package org.mendora.vo;

import java.util.Optional;

public enum AllProcessAction {
    REFRESH(0, "refresh"),
    RESTART_ALL(1, "restartall"),
    STOP_ALL(2, "stopall")
    ,;

    public final int code;

    public final String name;

    AllProcessAction(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static Optional<AllProcessAction> valOf(int code) {
        for (AllProcessAction action : values()) {
            if (action.code == code) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }
}
