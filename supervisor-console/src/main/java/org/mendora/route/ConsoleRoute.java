package org.mendora.route;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mendora.facade.RequestRouting;
import org.mendora.facade.RespCode;
import org.mendora.facade.Route;
import org.mendora.facade.RouteFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route("/console")
@Slf4j
public class ConsoleRoute implements RouteFactory {
	private String uploadDir = System.getProperty("uploadDir");

	@Builder
	private static class ProcessInfo {
		private String status;
		private String desc;
		private String name;
		private String action;
	}

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

	@RequestRouting(value = "/file", method = HttpMethod.POST, blocked = true)
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
			.map(file -> rxRename(file.uploadedFileName(), fileDir + "/" + file.fileName()).blockingGet())
			.subscribe(result -> succ(response, result), err -> log.error(err.getMessage(), err))
			.dispose();
	}

	@RequestRouting(value = "/process", blocked = true)
	public void loadingProcess(RoutingContext rtx) throws IOException {
		HttpServerResponse response = rtx.response();
		Document document = Jsoup.connect("http://localhost:9001/index.html").get();
		final Elements tbody = document.body().getElementsByTag("tbody");
		final Iterator<Element> trIterator = tbody.iterator();
		List<Element> trs = new ArrayList<>();
		while (trIterator.hasNext()) {
			trs.add(trIterator.next());
		}
		List<ProcessInfo> processInfos = trs.stream()
			.map(tr -> {
				final Iterator<Element> tdIterator = tr.children().iterator();
				List<Element> tds = new ArrayList<>();
				while (tdIterator.hasNext()) {
					tds.add(tdIterator.next());

				}
				return ProcessInfo.builder()
					.status(tds.get(0).html())
					.desc(tds.get(1).html())
					.name(tds.get(2).html())
					.action(tds.get(3).html())
					.build();
			}).collect(Collectors.toList());
		succ(response, processInfos);
	}

	private Maybe<Boolean> rxRename(String originPath, String destPath) {
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
