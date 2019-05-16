package org.mendora.route;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.mendora.facade.RequestRouting;
import org.mendora.facade.Route;
import org.mendora.facade.RouteFactory;

@Slf4j
@Route("/support")
public class SupportRoute implements RouteFactory {
    @RequestRouting(value = "/pay/notify", method = HttpMethod.POST)
    public void payCallBack(RoutingContext rtx) {
        JsonObject notifyData = rtx.getBodyAsJson();
        log.info("notify data: {}", notifyData.toString());
        HttpServerResponse response = rtx.response();
        response.end("{returnCode:\"Success\", returnMsg:\"Ok\"}");
    }
}
