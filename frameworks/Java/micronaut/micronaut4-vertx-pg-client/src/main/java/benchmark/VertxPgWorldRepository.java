package benchmark;

import benchmark.model.World;
import benchmark.repository.AsyncWorldRepository;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class VertxPgWorldRepository extends AbstractVertxSqlClientRepository implements AsyncWorldRepository {

    private PreparedStatement selectWorldStatement;
    private PreparedStatement updateWorldStatement;

    public VertxPgWorldRepository(PgConnection pgConnection) {
        super(pgConnection);
    }

    @PostConstruct
    public void init() {
        try {
            prepareSelectWorldQuery().toCompletableFuture().get();
            prepareUpdateWorldQuery().toCompletableFuture().get();
        } catch (Exception e) {
            // For testing, it will fail because there are no tables
        }
    }

    private CompletionStage<Void> prepareSelectWorldQuery() {
        return connection.prepare("SELECT * FROM world WHERE id = $1").toCompletionStage()
                .whenComplete((preparedStatement, throwable) -> selectWorldStatement = preparedStatement).
                thenApply(ignore -> null);
    }

    private CompletionStage<Void> prepareUpdateWorldQuery() {
        return connection.prepare("UPDATE world SET randomnumber = $2 WHERE id = $1").toCompletionStage()
                .whenComplete((preparedStatement, throwable) -> updateWorldStatement = preparedStatement).
                thenApply(ignore -> null);
    }

    private CompletionStage<Void> createTable() {
        return execute("DROP TABLE IF EXISTS World;")
                .thenCompose(ignore -> execute("CREATE TABLE World (id INTEGER NOT NULL,randomNumber INTEGER NOT NULL);"))
                .thenCompose(ignore -> prepareSelectWorldQuery())
                .thenCompose(ignore -> prepareUpdateWorldQuery());
    }

    @Override
    public CompletionStage<Void> initDb(Collection<World> worlds) {
        List<Tuple> data = worlds.stream().map(world -> Tuple.of(world.getId(), world.getRandomNumber())).toList();
        return createTable().thenCompose(unused -> executeBatch("INSERT INTO world VALUES ($1, $2);", data));
    }

    @Override
    public CompletionStage<World> findById(Integer id) {
        return selectWorldStatement.query().execute(Tuple.of(id)).map(rowSet -> {
            Row row = rowSet.iterator().next();
            return new World(row.getInteger(0), row.getInteger(1));
        }).toCompletionStage();
    }

    @Override
    public CompletionStage<List<World>> findByIds(List<Integer> ids) {
        List<World> worlds = new ArrayList<>();
        CompletableFuture<List<World>> result = new CompletableFuture<>();
        client.group(sqlClient -> {
            for (Integer id : ids) {
                selectWorldStatement.query().execute(Tuple.of(id), event -> {
                    if (event.failed()) {
                        result.completeExceptionally(event.cause());
                    } else {
                        Row row = event.result().iterator().next();
                        worlds.add(new World(row.getInteger(0), row.getInteger(1)));
                    }
                    if (ids.size() == worlds.size()) {
                        result.complete(worlds);
                    }
                });
            }
        });
        return result;
    }

    @Override
    public CompletionStage<Void> updateAll(Collection<World> worlds) {
        List<Tuple> data = worlds.stream().map(world -> Tuple.of(world.getId(), world.getRandomNumber())).toList();
        return updateWorldStatement.query().executeBatch(data).<Void>mapEmpty().toCompletionStage();
    }

}
