package org.mendora.vo;

import lombok.Data;

import java.util.Optional;

@Data
public class ProcessAction {
    private String processName;

    private int action;

    public enum Action {
        STAET(0, "start"),
        RESTART(1, "restart"),
        STOP(2, "stop"),
        CLEAR_LOG(3, "clearlog")
        ;

        public final int code;
        public final String name;

        Action(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Optional<Action> valOf(int code) {
            for (Action action : values()) {
                if (action.code == code) {
                    return Optional.of(action);
                }
            }
            return Optional.empty();
        }
    }
}
