package org.mendora;

import org.apache.logging.log4j.Level;

public class supervisorConsoleApplication {
    public static void main(String[] args) {
        System.setProperty("uploadDir", "/Users/menfre/Workbench/project/mendora-console/supervisor-console/file-uploads");
        WebModule.builder()
                .basePackageName("org.mendora.route")
                .fileCachingEnabled(false)
                .level(Level.INFO)
                .workerPoolSize(2)
                .logFileName("/Users/menfre/Workbench/project/mendora-console/supervisor-console/logs/supervisor-console.log")
                .build()
                .run();
    }
}
