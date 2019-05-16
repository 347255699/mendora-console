package org.mendora.route;

import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.mendora.facade.RequestRouting;
import org.mendora.facade.RespCode;
import org.mendora.facade.Route;
import org.mendora.facade.RouteFactory;
import org.mendora.vo.FileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Route("/doc")
public class DocRoute implements RouteFactory {
    private String dirPath = System.getProperty("uploadDir");

    public enum RespErrorCode implements RespCode {
        /**
         * 响应错误码
         */
        ERR_TARGET_DIR_NOT_EXISTS(30001, "The target dir not exists."),
        ERR_TARGET_DIR_IS_FILE(30002, "The target dir is a file.");

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

    @RequestRouting(value = "/dir/:root", blocked = true)
    public void loadFileDir(RoutingContext rtx) {
        String rootPath = dirPath.substring(0, dirPath.lastIndexOf("/") + 1);
        String rootDir = rtx.request().getParam("root");
        String dirPath = rootPath + rootDir;
        HttpServerResponse response = rtx.response();
        File dir = new File(dirPath);
        if (!dir.exists()) {
            fail(response, RespErrorCode.ERR_TARGET_DIR_NOT_EXISTS);
            return;
        }
        if (dir.isFile()) {
            fail(response, RespErrorCode.ERR_TARGET_DIR_IS_FILE);
            return;
        }
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            succ(response, new ArrayList<>(0));
            return;
        }
        List<FileInfo> fileInfos = Arrays.stream(files)
                .filter(file -> !file.getName().startsWith("."))
                .map(this::toFileInfo)
                .collect(Collectors.toList());
        succ(response, fileInfos);
    }

    private FileInfo toFileInfo(File file){
        FileInfo fi = new FileInfo();
        fi.setType(file.isFile() ? 1 : 0);
        fi.setName(file.getName());
        return fi;
    }
}