package org.mendora.route;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.mendora.facade.RequestRouting;
import org.mendora.facade.RespCode;
import org.mendora.facade.Route;
import org.mendora.facade.RouteFactory;
import org.mendora.vo.AllProcessAction;
import org.mendora.vo.ProcessAction;
import org.mendora.vo.ProcessInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

@Route("/console")
@Slf4j
public class ConsoleRoute implements RouteFactory {
    private String supervisorHost = StringUtils.isEmpty(System.getProperty("supervisorHost")) ?
            "http://localhost:9001" : System.getProperty("supervisorHost");

    public enum RespErrorCode implements RespCode {
        /**
         * 响应错误码
         */
        ERR_FILE_NOT_FOUND(20001, "The upload file not found."),
        ERR_ACTION_NOT_SUPPORTED(20002, "The acton code not supported.");

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
    public void loadProcess(RoutingContext rtx) throws IOException {
        HttpServerResponse response = rtx.response();
        Document document = Jsoup.connect(supervisorHost.concat("/index.html")).get();
        loadProcess(document)
                .subscribe(arr -> succ(response, arr), err -> log.error(err.getMessage(), err))
                .dispose();
    }

    @RequestRouting(value = "/process", method = HttpMethod.POST, blocked = true)
    public void oprateProcess(RoutingContext rtx) throws IOException {
        ProcessAction processAction = rtx.getBodyAsJson().mapTo(ProcessAction.class);
        HttpServerResponse response = rtx.response();
        Optional<ProcessAction.Action> actionOptional = ProcessAction.Action.valOf(processAction.getAction());
        if (!actionOptional.isPresent()) {
            fail(response, RespErrorCode.ERR_ACTION_NOT_SUPPORTED);
            return;
        }
        ProcessAction.Action action = actionOptional.get();

        String url = supervisorHost
                .concat("/index.html?processname=")
                .concat(processAction.getProcessName())
                .concat("&action=")
                .concat(action.name);

        loadProcess(Jsoup.connect(url).get())
                .flatMapObservable(Observable::fromIterable)
                .filter(info -> info.getName().equals(processAction.getProcessName()))
                .subscribe(info -> succ(response, info), err -> log.error(err.getMessage(), err))
                .dispose();
    }

    @RequestRouting(value = "/process/all/:actionCode", blocked = true)
    public void oprateAllProcess(RoutingContext rtx) throws IOException {
        HttpServerResponse response = rtx.response();
        int actionCode = Integer.valueOf(rtx.request().getParam("actionCode"));
        Optional<AllProcessAction> allProcessAction = AllProcessAction.valOf(actionCode);
        if (!allProcessAction.isPresent()) {
            fail(response, RespErrorCode.ERR_ACTION_NOT_SUPPORTED);
            return;
        }
        AllProcessAction action = allProcessAction.get();
        Document document = Jsoup.connect(supervisorHost.concat("/index.html?action=").concat(action.name)).get();
        loadProcess(document)
                .subscribe(arr -> succ(response, arr), err -> log.error(err.getMessage(), err))
                .dispose();
    }

    @RequestRouting(value = "/process/log/tail/:processName", blocked = true)
    public void logtail(RoutingContext rtx) {
        HttpServerResponse response = rtx.response();
        String processName = rtx.request().getParam("processName");
        response.putHeader("Location", supervisorHost.concat("/logtail/").concat(processName))
                .setStatusCode(HttpResponseStatus.FOUND.code())
                .end();
    }

    private Single<ArrayList<ProcessInfo>> loadProcess(Document document) {
        return Single.just(document)
                .map(Document::body)
                .map(body -> body.getElementsByTag("tr"))
                .flatMapObservable(Observable::fromIterable)
                .skip(1)
                .map(this::toProcessInfo)
                .collect(ArrayList::new, ArrayList::add);
    }

    private ProcessInfo toProcessInfo(Element tdEle) {
        ProcessInfo processInfo = new ProcessInfo();
        processInfo.setState(ProcessInfo.State.valOf(tdEle.child(0).child(0).text()).state);
        processInfo.setDescription(tdEle.child(1).child(0).text());
        processInfo.setName(tdEle.child(2).child(0).text());
        processInfo.setLoading(false);
        return processInfo;
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
