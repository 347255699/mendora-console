package org.mendora.route;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.mendora.facade.RequestRouting;
import org.mendora.facade.RespCode;
import org.mendora.facade.Route;
import org.mendora.facade.RouteFactory;

import java.io.File;
import java.util.Set;

@Route("/console")
@Slf4j
public class ConsoleRoute implements RouteFactory {
	private String uploadDir = System.getProperty("uploadDir");

	public enum RespErrorCode implements RespCode {
		/**
		 * 响应错误码
		 */
		ERR_FILE_NOT_FOUND(20001, "The upload file not found."),
		;

		final public int code;
		final public String msg;

		RespErrorCode(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		@Override
		public int code() {
			return code;
		}

		@Override
		public String msg() {
			return msg;
		}
	}

	@RequestRouting(value = "/jar", method = HttpMethod.POST, blocked = true)
	public void uploadJar(RoutingContext rtx) {
		HttpServerRequest request = rtx.request();
		HttpServerResponse response = rtx.response();
		String fileDir = request.getFormAttribute("fileDir");
		Set<FileUpload> fileUploads = rtx.fileUploads();
		if (fileUploads.size() == 0) {
			fail(response, RespErrorCode.ERR_FILE_NOT_FOUND);
			return;
		}
		Single.just(fileUploads)
			.filter(set -> set.size() == 1)
			.map(set -> set.iterator().next())
			.map(file -> reName(file.uploadedFileName(), fileDir + "/" + file.fileName()))
			.subscribe(result -> succ(response, result), err -> log.error(err.getMessage(), err))
			.dispose();
	}

	private Maybe<Boolean> reName(String originPath, String destPath) {
		File origin = new File(originPath);
		String destDirName = destPath.substring(0, destPath.lastIndexOf("/") + 1);
		return Single.just(new File(destDirName))
			.filter(dirFile -> dirFile.exists() || dirFile.mkdirs())
			.map(dirFile -> destPath)
			.map(File::new)
			.filter(destFile -> destFile.exists() || destFile.createNewFile())
			.map(origin::renameTo);
	}
}
