package org.workspace7.knative.eventing;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventBuilder;
import io.cloudevents.http.reactivex.vertx.VertxCloudEvents;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

/**
 * Kafka Consumer Source
 *
 */
public class KafkaConsumerSource extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerSource.class);

    public void start() {
        LOGGER.info("KafkaConsumerSource Starting ...");
        // to make this event source running
        Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        // used for health checks
        router.get("/").handler(r -> {
            r.response().setStatusCode(200).end("OK");
        });

        vertx.createHttpServer().requestHandler(router).listen(8080);
        LOGGER.info("KafkaConsumerSource Started ...");

        sendCloudEvent();
    }

    private void sendCloudEvent() {
        Map<String, String> env = System.getenv();
        LOGGER.debug("ENV:{}", env);

        for (int i = 0; i < 100; i++) {
            HttpClientRequest request = vertx.createHttpClient().postAbs(env.get("SINK"));
            request.handler(res -> {
                LOGGER.info("Cloud Event Status Code:{}", res.statusCode());
                LOGGER.info("Cloud Event Status message:{}", res.statusMessage());
            });
            JsonObject data = new JsonObject();
            env.forEach((k, v) -> {
                data.put(k, v);
            });

            CloudEvent<String> ce = new CloudEventBuilder<String>().id(UUID.randomUUID().toString())
                    .source(URI.create("http://workspace7.org")).type("testevent")
                    .specVersion("0.2").data("{\"name\": \"krishna\"}").build();
            VertxCloudEvents.create().writeToHttpClientRequest(ce, request);
            // request.end(); - this causes illegal state exception
        }

    }
}
