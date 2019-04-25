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
		Vertx vertx = vertx(false, 10);
		LoggerModule.builder()
			.logLevel(Level.DEBUG)
			.build()
			.run();
		System.setProperty("uploadDir", "/webserver/other/file-uploads");
		WebModule.builder()
			.webRoot("/webserver/other/")
			.port(83)
			.build()
			.run(vertx, "org.mendora.route");
	}
}
