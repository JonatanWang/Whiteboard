import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * When a user connects websocket, set a consumer for each painting the the user is involved.
 * Client specifies which painting he is drawing to.
 * Client adds a new painting to the consumer list, when it is created.
 */
public class ServicesVerticle extends AbstractVerticle {

    private Map<String, PaintClient> clients = new HashMap<String, PaintClient>();

    @Override
    public void start() {
        /**
         * Routinely create:
         * 1. vertx(leave it to Starter.java
         * 2. httpServer
         * 3. router
         * 4. thymeleafTemplateEngine
         * 5. templateResolver
         * 6. eventBus
         * 7. dataAccess
         */
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        EventBus eventBus = vertx.eventBus();
        DataAccess dataAccess = DataAccess.create(vertx);


        // configurations
        templateResolver.setPrefix("templates");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("html5");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

        // setting for httpServer
        httpServer.websocketHandler(serverWebSocket -> {

            clients.put(serverWebSocket.textHandlerID(), new PaintClient());

            serverWebSocket.closeHandler(handler -> {
                clients.remove(serverWebSocket.textHandlerID()).removeAll();
            });

            // msg from client
            serverWebSocket.frameHandler(frame -> {
                System.out.println(frame.textData());
                JsonObject jsonObject = new JsonObject(frame.textData());

                switch (jsonObject.getString("type")) {

                    case "draw":
                        JsonObject object = new JsonObject();
                        // How to save the drawing image changes??
                        jsonObject.put("image", jsonObject.getBinary("image"));
                        // Publish the real-time drawing events to all mutually compiling users
                        //how to get real-time draws??
                        eventBus.publish("draw.painting." + jsonObject.getInteger("paintId"),
                                object.toString());
                        break;

                    case "paintings":
                        // List the paintings corresponding userId
                        DBServices.getPaintings(dataAccess.getJdbcClient(),
                                jsonObject.getInteger("userId"),
                                resultSet -> {
                                // Todo-handle the query result: painting list
                                });
                        break;

                    case "users":
                        DBServices.getUsers(dataAccess.getJdbcClient(),
                                jsonObject.getInteger("userId"),
                                resultSet -> {
                            // Todo...handler the query result: user list
                                });
                        break;

                    case "openPainting":
                        // Open the selected painting by paintId, add a handler??
                        int id = jsonObject.getInteger("paintId");
                        PaintClient client = clients.get(serverWebSocket.textHandlerID());
                        if(!client.isInvited(id)){

                            MessageConsumer<String> consumer = eventBus.consumer("draw.painting." + id);
                            client.addConsumer(id, consumer);

                            consumer.handler( message->{
                                JsonObject msg = new JsonObject(message.body());
                                msg.put("type", "image");
                                serverWebSocket.writeFinalTextFrame(msg.toString());
                            });
                        }
                        // Open the selected painting by paintId, add a handler??
                        DBServices.getPainting(dataAccess.getJdbcClient(),
                                jsonObject.getInteger("paintId"));
                        break;

                    case "invite":
                        DBServices.updateUserPaint(dataAccess.getJdbcClient(),
                                jsonObject.getInteger("paintId"),
                                jsonObject.getInteger("invitedUserId"));
                        break;

                    case "create":
                        DBServices.createPainting(dataAccess.getJdbcClient(),
                                jsonObject.getInteger("userId"),
                                jsonObject.getString("paintName"),
                                resultSet -> {
                            JsonObject image = resultSet.getRows().get(0);
                            image.put("type", "newPainting");
                            serverWebSocket.writeFinalTextFrame(image.toString()); //Data-type wrong?
                                });
                        break;
                }
            });
        }).requestHandler(request -> {
            request.response().sendFile("whiteboard.html");
        }).listen(8080);
    }
}
