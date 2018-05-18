import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class AccountVerticle extends AbstractVerticle {

    @Override
    public void start() {

        /**
         * Routinely create:
         * 1. vertx(leave it to Starter.java
         * 2. httpServer
         * 3. router
         * 4. thymeleafTemplateEngine
         * 5. templateResolver
         */
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

        templateResolver.setPrefix("templates");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("html5");
        templateEngine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);
        router.route("/register").handler(BodyHandler.create());
        router.get("/register").handler(routingContext -> {
            templateEngine.render(routingContext, "/register", res -> {
                if (res.succeeded()) {
                    routingContext.response().putHeader("content-type", "text/html")
                            .end(res.result());
                }else {
                    routingContext.fail(res.cause());
                }
            });
        });


        router.post("/register").handler(routingContext -> {

            String email = routingContext.request().getParam("email");
            String password = routingContext.request().getParam("password");
            String name = routingContext.request().getParam("name");
            /**
            routingContext.response().putHeader("content-type", "text/html")
                    .end("Email: " + email
                    + ", Password: " + password
                    + ", Name: " + name);*/
            routingContext.response().sendFile("login.html");

            DataAccess dataAccess = DataAccess.create(vertx);
            dataAccess.getJdbcClient().getConnection(res -> {
                if (res.succeeded()) {
                    SQLConnection connection = res.result();
                    String update = "INSERT INTO user (email, password, name) values (?, ?, ?)";
                    JsonArray params = new JsonArray().add(email).add(password).add(name);
                    connection.updateWithParams(update, params,
                            res2 -> {
                                if (res2.succeeded()) {
                                    UpdateResult updateResult = res2.result();
                                    System.out.printf("No. of rows updated: " + updateResult.getUpdated());
                                } else {
                                    routingContext.fail(res2.cause());
                                }
                            });
                }
            });
        });
        httpServer.requestHandler(router::accept).listen(8080);
    }
}
