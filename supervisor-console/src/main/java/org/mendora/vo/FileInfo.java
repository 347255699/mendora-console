package org.mendora.vo;

import lombok.Data;

@Data
public class FileInfo {
    private String name;

    private int type;

    public enum Type {
        DIR(0, "director"),
        FILE(1, "file");

        final public int type;
        final public String name;

        Type(int type, String name) {
            this.type = type;
            this.name = name;
        }

        public static Type valOf(int type) {
            return type == 0 ? DIR : FILE;
        }
    }
}
