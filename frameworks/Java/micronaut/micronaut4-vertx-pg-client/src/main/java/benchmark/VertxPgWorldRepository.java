package benchmark;

import benchmark.model.World;
import benchmark.repository.AsyncWorldRepository;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class VertxPgWorldRepository extends AbstractVertxSqlClientRepository implements AsyncWorldRepository {

    public VertxPgWorldRepository(PgPool pgPool) {
        super(pgPool);
    }

    private CompletionStage<Void> createTable() {
        return execute("DROP TABLE IF EXISTS World;")
                .thenCompose(ignore -> execute("CREATE TABLE World (id INTEGER NOT NULL,randomNumber INTEGER NOT NULL);"));
    }

    @Override
    public CompletionStage<Void> initDb(Collection<World> worlds) {
        List<Tuple> data = worlds.stream().map(world -> Tuple.of(world.getId(), world.getRandomNumber())).toList();
        return createTable().thenCompose(unused -> executeBatch("INSERT INTO world VALUES ($1, $2);", data));
    }

    @Override
    public CompletionStage<World> findById(Integer id) {
        return pool.preparedQuery("SELECT * FROM world WHERE id = $1").execute(Tuple.of(id)).map(rowSet -> {
            Row row = rowSet.iterator().next();
            return new World(row.getInteger(0), row.getInteger(1));
        }).toCompletionStage();
    }

    @Override
    public CompletionStage<List<World>> findByIds(List<Integer> ids) {
        return pool.withTransaction(sqlConnection -> {
            Promise<List<World>> promise = Promise.promise();
            List<World> worlds = new ArrayList<>(ids.size());
            PreparedQuery<RowSet<Row>> preparedQuery = sqlConnection.preparedQuery("SELECT * FROM world WHERE id = $1");
            for (Integer id : ids) {
                preparedQuery.execute(Tuple.of(id), event -> {
                    if (event.failed()) {
                        promise.fail(event.cause());
                    } else {
                        Row row = event.result().iterator().next();
                        worlds.add(new World(row.getInteger(0), row.getInteger(1)));
                    }
                    if (ids.size() == worlds.size()) {
                        promise.complete(worlds);
                    }
                });
            }
            return promise.future();
        }).toCompletionStage();
    }

    @Override
    public CompletionStage<Void> updateAll(Collection<World> worlds) {
        List<Tuple> data = worlds.stream().map(world -> Tuple.of(world.getId(), world.getRandomNumber())).toList();
        return pool.preparedQuery("UPDATE world SET randomnumber = $2 WHERE id = $1").executeBatch(data).<Void>mapEmpty().toCompletionStage();
    }

}
