package benchmark.repository;

import benchmark.model.World;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.List;

public interface ReactiveWorldRepository {

    Publisher<Void> initDb(Collection<World> worlds);

    Publisher<World> findById(Integer id);

    Publisher<List<World>> findByIds(Integer[] ids);

    Publisher<List<World>> updateAll(Integer[] ids, Integer[] randoms);

}
