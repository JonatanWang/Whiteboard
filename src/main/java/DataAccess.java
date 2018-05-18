import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;

public class DataAccess {

    private static DataAccess dataAccess;
    private static JDBCClient jdbcClient;
    private static JsonObject config;

    static {
        config = new JsonObject();
        config.put("url", "jdbc:mysql://localhost:3306/vertx?useSSL=false");
        config.put("driver_class", "com.mysql.jdbc.Driver");
        config.put("user", "root");
        config.put("password", "sesame");
    }

    public static DataAccess create(Vertx vertx) {
        if (dataAccess == null) {
            synchronized (DataAccess.class) {
                if (dataAccess == null) {
                    dataAccess = new DataAccess();
                    dataAccess.init(vertx);
                }
            }
        }
        return dataAccess;
    }

    private void init(Vertx vertx) {
        jdbcClient = JDBCClient.createShared(vertx, config);
    }

    public JDBCClient getJdbcClient() {
        return jdbcClient;
    }
}
