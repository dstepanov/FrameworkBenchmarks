package benchmark;

import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.SqlClientInternal;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AbstractVertxSqlClientRepository {

    protected final Pool pool;
    protected final SqlClientInternal client;

    public AbstractVertxSqlClientRepository(PgPool pgPool) {
        this.pool = pgPool;
        this.client = (SqlClientInternal) pgPool;
    }

    protected CompletionStage<Void> execute(String sql) {
        return client.preparedQuery(sql).collecting(Collectors.toList()).execute().<Void>mapEmpty().toCompletionStage();
    }

    protected CompletionStage<Void> executeBatch(String sql, List<Tuple> data) {
        return client.preparedQuery(sql).executeBatch(data).<Void>mapEmpty().toCompletionStage();
    }

}
