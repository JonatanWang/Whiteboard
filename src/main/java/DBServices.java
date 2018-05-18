import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

public class DBServices {
    //CRUD methods
    // create a painting in the paint & user_paint tables
    public static void createPainting(JDBCClient jdbcClient, int userId, String paintName, Handler<ResultSet> handler) {
        jdbcClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                String queryPaint = "INSERT INTO paint (name, image) VALUES (?, null)";
                JsonArray paramsPaint = new JsonArray().add(paintName);
                connection.updateWithParams(queryPaint, paramsPaint, res2 -> {
                    if (res2.succeeded()) {
                        // get the latest paint.id in the paint table
                        String queryScope = "SELECT TOP 1 * FROM paint ORDER BY id DESC;";
                        connection.query(queryScope, res3 -> {
                            int paintId = res3.result().getResults().get(0).getInteger(0);
                            updateUserPaint(jdbcClient, paintId, userId);
                            handler.handle(res3.result());
                        });
                    } else {
                        res2.cause().printStackTrace();
                    }
                    connection.close();
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    // update/change a painting in the paint table
    public static void updatePainting(JDBCClient jdbcClient, int paintId, Handler<ResultSet> handler) {

    }

    // update user_paint table, when a new painting is created or a new user joins the mutual painting
    public static void updateUserPaint(JDBCClient jdbcClient, int paintId, int userId) {
        jdbcClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();
                String query = "INSERT INTO user_paint (paintId, userId) VALUES (?, ?)";
                JsonArray params = new JsonArray().add(paintId).add(userId);
                connection.updateWithParams(query, params, res2 -> {connection.close();
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    // delete a painting in the paint & user_paint tables
    public static void deletePainting(JDBCClient jdbcClient, int paintId, Handler<ResultSet> handler) {

    }

    // get a specific painting that a user creates / compiles
    public static void getPainting(JDBCClient jdbcClient, int paintId) {

    }

    // get all paintings that a user creates / compiles
    public static void getPaintings(JDBCClient jdbcClient, int userId, Handler<io.vertx.ext.sql.ResultSet> handler) {

        jdbcClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection sqlConnection = res.result();
                String query = "SELECT paintId, paintName FROM paint" +
                        "INNER JOIN user_paint ON user_paint.paintId = paint.id " +
                        "INNER JOIN user ON user_paint.userId = user.id " +
                        "WHERE user_paint.userId = ?";
                JsonArray params = new JsonArray().add(userId);
                sqlConnection.queryWithParams(query, params, res2 -> {
                    if (res2.succeeded()) {
                        handler.handle(res2.result());
                    } else {
                        res2.cause().printStackTrace();
                    }
                    sqlConnection.close();
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }

    // get all users in database
    public static void getUsers(JDBCClient jdbcClient, int userId, Handler<io.vertx.ext.sql.ResultSet> handler) {
        jdbcClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection sqlConnection = res.result();
                String query = "SELECT * FROM user WHERE id <> ?";
                JsonArray params = new JsonArray().add(userId);
                sqlConnection.queryWithParams(query, params, res2 -> {
                    if (res2.succeeded()) {
                        handler.handle(res2.result());
                    } else {
                        res2.cause().printStackTrace();
                    }
                });
            } else {
                res.cause().printStackTrace();
            }
        });
    }
}
