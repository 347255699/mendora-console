package org.mendora;

import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.Level;

/**
 * Supervisor控制台应用
 *
 * @author menfre
 */
public class SupervisorConsoleApplication extends VertxApplicationInit{
	public static Vertx vertx;

	public static void main(String[] args) {
		LoggerModule.builder()
			.logLevel(Level.DEBUG)
			.build()
			.run();
		vertx = vertx(false, 10);
		System.setProperty("uploadDir", "/webserver/other/file-uploads");
		WebModule.builder()
			.webRoot("/webserver/other/")
			.port(8080)
			.build()
			.run(vertx, "org.mendora.route");
	}
}
