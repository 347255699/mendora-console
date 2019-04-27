package org.mendora;

import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.Level;

/**
 * Supervisor控制台应用
 *
 * @author menfre
 */
public class SupervisorConsoleApplication extends VertxApplicationInit{
	public static void main(String[] args) {
		LoggerModule.builder()
			.logLevel(Level.DEBUG)
			.build()
			.run();
		Vertx vertx = vertx(false, 10);
		System.setProperty("uploadDir", "/Users/menfre/Workbench/deploy/file-uploads");
		System.setProperty("supervisorHost", "http://localhost:9001");
		WebModule.builder()
			.webRoot("/Users/menfre/Workbench/deploy/html")
			.port(8080)
			.build()
			.run(vertx, "org.mendora.route");
	}
}
