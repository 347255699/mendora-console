package org.mendora;

import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.Level;

/**
 * Supervisor控制台应用
 *
 * @author menfre
 */
public class SupervisorConsoleApplication {
	public static void main(String[] args) {
		LoggerModule.builder()
			.logLevel(Level.DEBUG)
			.build()
			.run();
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		VertxOptions vertxOptions = new VertxOptions()
			.setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(false))
			.setWorkerPoolSize(10);
		Vertx vertx = Vertx.vertx(vertxOptions);
		System.setProperty("uploadDir", "/webserver/other/file-uploads");
		WebModule.builder()
			.webRoot("/webserver/other/")
			.port(83)
			.build()
			.run(vertx, "org.mendora.route");
	}
}
