package benchmark.repository;

import benchmark.model.World;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface AsyncWorldRepository {

    CompletionStage<Void> initDb(Collection<World> worlds);

    CompletionStage<World> findById(Integer id);

    CompletionStage<List<World>> findByIds(List<Integer> ids);

    CompletionStage<Void> updateAll(Collection<World> worlds);

}
