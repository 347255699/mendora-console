package org.mendora;

import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.Level;

/**
 * Supervisor控制台应用
 *
 * @author menfre
 */
public class SupervisorConsoleApplication extends VertxApplicationInit {
    private static final String ROOT_DIR = "/Users/menfre/workbench/deploy";
    private static final String SUPERVISOR_HOST = "localhost";

    public static void main(String[] args) {
        LoggerModule.builder()
                .logLevel(Level.DEBUG)
                .build()
                .run();
        Vertx vertx = vertx(false, 10);
        System.setProperty("uploadDir", ROOT_DIR + "/file-uploads");
        System.setProperty("supervisorHost", "http://" + SUPERVISOR_HOST + ":9001");
        WebModule.builder()
                .webRoot(ROOT_DIR + "/html")
                .port(9002)
                .build()
                .run(vertx, "org.mendora.route");
    }
}
