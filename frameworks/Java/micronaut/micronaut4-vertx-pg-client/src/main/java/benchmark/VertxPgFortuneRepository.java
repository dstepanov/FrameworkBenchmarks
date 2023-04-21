package benchmark;

import benchmark.model.Fortune;
import benchmark.repository.AsyncFortuneRepository;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

@Singleton
public class VertxPgFortuneRepository extends AbstractVertxSqlClientRepository implements AsyncFortuneRepository {

    private PreparedStatement selectFortune;

    public VertxPgFortuneRepository(PgConnection pgConnection) {
        super(pgConnection);
    }

    @PostConstruct
    public void init() {
        try {
            prepareQuery().toCompletableFuture().get();
        } catch (Exception e) {
            // For testing, it will fail because there are no tables
        }
    }

    private CompletionStage<Void> prepareQuery() {
        return connection.prepare("SELECT * FROM Fortune").toCompletionStage()
                .whenComplete((preparedStatement, throwable) -> selectFortune = preparedStatement)
                .thenApply(ignore -> null);
    }

    private CompletionStage<Void> createTable() {
        return execute("DROP TABLE IF EXISTS Fortune;")
                .thenCompose(ignore -> execute("CREATE TABLE Fortune (id INTEGER NOT NULL,message VARCHAR(255) NOT NULL);"))
                .thenCompose(unused -> prepareQuery());
    }

    @Override
    public CompletionStage<Void> initDb(Collection<Fortune> fortunes) {
        List<Tuple> data = fortunes.stream().map(fortune -> Tuple.of(fortune.getId(), fortune.getMessage())).toList();
        return createTable().thenCompose(unused -> executeBatch("INSERT INTO Fortune VALUES ($1, $2);", data));
    }

    @Override
    public CompletionStage<List<Fortune>> findAll() {
        return selectFortune.query()
                .collecting(Collectors.mapping(row -> new Fortune(row.getInteger(0), row.getString(1)), Collectors.toList()))
                .execute()
                .map(SqlResult::value)
                .toCompletionStage();
    }

}
