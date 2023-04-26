package benchmark;

import benchmark.model.Fortune;
import benchmark.repository.AsyncFortuneRepository;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class VertxPgAsyncFortuneRepository extends AbstractAsyncVertxSqlClientRepository implements AsyncFortuneRepository {

    public VertxPgAsyncFortuneRepository(PgPool pgPool) {
        super(pgPool);
    }

    private CompletionStage<Void> createTable() {
        return execute("DROP TABLE IF EXISTS Fortune;")
                .thenCompose(ignore -> execute("CREATE TABLE Fortune (id INTEGER NOT NULL,message VARCHAR(255) NOT NULL);"));
    }

    @Override
    public CompletionStage<Void> initDb(Collection<Fortune> fortunes) {
        List<Tuple> data = fortunes.stream().map(fortune -> Tuple.of(fortune.getId(), fortune.getMessage())).toList();
        return createTable().thenCompose(unused -> executeBatch("INSERT INTO Fortune VALUES ($1, $2);", data));
    }

    @Override
    public CompletionStage<List<Fortune>> findAll() {
        return pool.preparedQuery("SELECT * FROM Fortune")
                .collecting(Collectors.mapping(row -> new Fortune(row.getInteger(0), row.getString(1)), Collectors.toList()))
                .execute()
                .map(SqlResult::value)
                .toCompletionStage();
    }

}
