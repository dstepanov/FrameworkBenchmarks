package benchmark;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AbstractVertxSqlClientRepository {

    protected final PgConnection connection;
    protected final SqlClientInternal client;

    public AbstractVertxSqlClientRepository(PgConnection connection) {
        this.connection = connection;
        this.client = (SqlClientInternal) connection;
    }

    protected CompletionStage<Void> execute(String sql) {
        return client.preparedQuery(sql).collecting(Collectors.toList()).execute().<Void>mapEmpty().toCompletionStage();
    }

    protected CompletionStage<Void> executeBatch(String sql, List<Tuple> data) {
        return client.preparedQuery(sql).executeBatch(data).<Void>mapEmpty().toCompletionStage();
    }

}
