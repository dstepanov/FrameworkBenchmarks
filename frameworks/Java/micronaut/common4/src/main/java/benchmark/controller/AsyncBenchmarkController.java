package benchmark.controller;

import benchmark.model.Fortune;
import benchmark.model.World;
import benchmark.repository.AsyncFortuneRepository;
import benchmark.repository.AsyncWorldRepository;
import benchmark.repository.ReactiveFortuneRepository;
import benchmark.repository.ReactiveWorldRepository;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import views.fortunes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.Comparator.comparing;

@Requires(beans = {AsyncFortuneRepository.class, AsyncWorldRepository.class})
@Controller
public class AsyncBenchmarkController extends AbstractBenchmarkController {

    private final AsyncWorldRepository worldRepository;
    private final AsyncFortuneRepository fortuneRepository;

    public AsyncBenchmarkController(AsyncWorldRepository worldRepository,
                                    AsyncFortuneRepository fortuneRepository) {
        this.worldRepository = worldRepository;
        this.fortuneRepository = fortuneRepository;
    }

    @Get("/prepare-data-for-test")
    public CompletionStage<Void> prepareDataForTest() {
        return worldRepository.initDb(createWords()).thenAccept(unused -> fortuneRepository.initDb(createFortunes()));
    }

    // https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#single-database-query
    @Get("/db")
    @SingleResult
    public CompletionStage<World> db() {
        return worldRepository.findById(randomId());
    }

    // https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#multiple-database-queries
    @Get("/queries")
    @SingleResult
    public CompletionStage<List<World>> queries(@QueryValue String queries) {
        int count = parseQueryCount(queries);
        List<Integer> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(randomId());
        }
        return worldRepository.findByIds(ids);
    }

    // https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#fortunes
    @Get(value = "/fortunes", produces = "text/html;charset=utf-8")
    @SingleResult
    public CompletionStage<HttpResponse<String>> fortune() {
        return fortuneRepository.findAll().thenApply(fortuneList -> {
            List<Fortune> all = new ArrayList<>(fortuneList.size() + 1);
            all.add(new Fortune(0, "Additional fortune added at request time."));
            all.addAll(fortuneList);
            all.sort(comparing(Fortune::getMessage));
            String body = fortunes.template(all).render().toString();
            return HttpResponse.ok(body).contentType("text/html;charset=utf-8");
        });
    }

    // https://github.com/TechEmpower/FrameworkBenchmarks/wiki/Project-Information-Framework-Tests-Overview#database-updates
    @Get("/updates")
    @SingleResult
    public CompletionStage<List<World>> updates(@QueryValue String queries) {
        return queries(queries).thenCompose(worlds -> {
            for (World world : worlds) {
                world.setRandomNumber(randomWorldNumber());
            }
            worlds.sort(Comparator.comparingInt(World::getId)); // Avoid deadlock
            return worldRepository.updateAll(worlds).thenApply(unused -> worlds);
        });
    }

}
