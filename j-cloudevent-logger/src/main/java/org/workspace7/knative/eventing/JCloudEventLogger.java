package org.workspace7.knative.eventing;

import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCloudEventLogger extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCloudEventLogger.class);

    @Override
    public void start() throws Exception {

        LOGGER.info("Starting Java Cloud Event Logger");

        vertx.createHttpServer().requestHandler(req -> VertxCloudEvents.create()
                .rxReadFromRequest(req).subscribe((receivedEvent, throwable) -> {
                    if (receivedEvent != null) {
                        // I got a CloudEvent object:
                        LOGGER.info("Event Data" + receivedEvent.getData().get());
                    }
                })).rxListen(8080).subscribe(server -> {
                    LOGGER.info("Started Java Cloud Event Logger");
                });
    }
}
