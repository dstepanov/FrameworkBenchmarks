package benchmark;

import benchmark.model.World;
import benchmark.repository.ReactiveWorldRepository;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class VertxPgWorldRepository extends AbstractVertxSqlClientRepository implements ReactiveWorldRepository {

    public VertxPgWorldRepository(Pool client) {
        super(client);
    }

    private Mono<Void> createTable() {
        return execute("DROP TABLE IF EXISTS World;").then(execute("CREATE TABLE World (id INTEGER NOT NULL,randomNumber INTEGER NOT NULL);").then());
    }

    @Override
    public Mono<Void> initDb(Collection<World> worlds) {
        List<Tuple> data = worlds.stream().map(world -> Tuple.of(world.getId(), world.getRandomNumber())).collect(Collectors.toList());
        return createTable().then(executeBatch("INSERT INTO world VALUES ($1, $2);", data).then());
    }

    @Override
    public Publisher<World> findById(Integer id) {
        Tuple tuple = Tuple.of(id);
        return asMono(
                client.withConnection(sqlConnection -> sqlConnection.preparedQuery("SELECT * FROM world WHERE id = $1")
                        .execute(tuple)
                        .map(rowSet -> {
                            Row row = rowSet.iterator().next();
                            return new World(row.getInteger(0), row.getInteger(1));
                        }))
        );
    }

    @Override
    public Publisher<List<World>> findByIds(Integer[] ids) {
        return asMono(
                client.withConnection(sqlConnection -> findByIds(sqlConnection, ids))
        );
    }

    private Future<List<World>> findByIds(SqlConnection sqlConnection, Integer[] ids) {
        List<Future> futures = new ArrayList<>(ids.length);
        for (Integer id : ids) {
            futures.add(
                    sqlConnection.preparedQuery("SELECT * FROM world WHERE id = $1").execute(Tuple.of(id)).map(rowSet -> {
                        Row row = rowSet.iterator().next();
                        return new World(row.getInteger(0), row.getInteger(1));
                    })
            );
        }
        return CompositeFuture.all(futures).map(CompositeFuture::list);
    }

    private <T> Mono<T> asMono(Future<T> future) {
        return Mono.fromFuture(future.toCompletionStage().toCompletableFuture());
    }

    @Override
    public Publisher<List<World>> updateAll(Integer[] ids, Integer[] randoms) {
        return asMono(
                client.withConnection(sqlConnection -> findByIds(sqlConnection, ids).flatMap(worlds -> {
                    worlds.sort(Comparator.comparingInt(World::getId)); // Avoid deadlock

                    List<Tuple> data = new ArrayList<>(worlds.size());
                    int index = 0;
                    for (World world : worlds) {
                        data.add(Tuple.of(world.getId(), randoms[index++]));
                    }

                    return sqlConnection.preparedQuery("UPDATE world SET randomnumber = $2 WHERE id = $1")
                            .executeBatch(data)
                            .map(worlds);
                }))
        );
    }

}
