package org.mendora.route;

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
import java.io.IOException;
import java.util.Set;

@Route("/console")
@Slf4j
public class ConsoleRoute implements RouteFactory {
    private String uploadDir = System.getProperty("uploadDir");

    public enum RespErrCode implements RespCode{
        ERR_FILE_NOT_FOUND(20001, "The upload file not found."),
        ;

        final public int code;
        final public String msg;

        RespErrCode(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int code() {
            return code;
        }

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
        if (fileUploads.size() == 1) {
            FileUpload fileUpload = fileUploads.iterator().next();
            String tempFileName = fileUpload.uploadedFileName();
            String fileName = fileUpload.fileName();
            succ(response, reName(tempFileName, fileDir + "/" + fileName));
        } else {
            fail(response, RespErrCode.ERR_FILE_NOT_FOUND);
        }
    }

    private boolean reName(String originPath, String destPath) {
        File origin = new File(originPath);
        String destDirName = destPath.substring(0, destPath.lastIndexOf("/") + 1);
        File destDirFile = new File(destDirName);
        if (destDirFile.exists() || destDirFile.mkdirs()) {
            File dest = new File(destPath);
            try {
                if (dest.exists() || dest.createNewFile()) {
                    return origin.renameTo(dest);
                } else {
                    return false;
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return false;
    }
}
