package org.mendora.vo;

import lombok.Data;

@Data
public class ProcessInfo {
    private int State;

    private String description;

    private String name;

    private boolean loading;

    public enum State{
        STOPPED (0, "stopped"),
        STARTING(10, "starting"),
        RUNNING(20, "running"),
        BACKOFF (30, "backoff"),
        STOPPING(40, "stopping"),
        EXITED(100, "exited"),
        FATAL(200, "fatal"),
        UNKNOWN(1000, "unknow")
        ,;

        public final int state;
        public final String name;
        State(int state, String name){
            this.state = state;
            this.name = name;
        }

        public static State valOf(String name){
            for(State state : values()){
                if(state.name.equals(name)){
                    return state;
                }
            }
            return UNKNOWN;
        }
    }
}
