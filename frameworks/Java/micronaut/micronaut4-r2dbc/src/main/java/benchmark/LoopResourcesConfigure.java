package benchmark;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import jakarta.inject.Singleton;
import reactor.netty.resources.LoopResources;

@Singleton
public class LoopResourcesConfigure implements BeanCreatedEventListener<ConnectionFactoryOptions.Builder> {

    @Override
    public ConnectionFactoryOptions.Builder onCreated(BeanCreatedEvent<ConnectionFactoryOptions.Builder> event) {
        return event.getBean().option(Option.valueOf("loopResources"), LoopResources.create("r2dbc", LoopResources.DEFAULT_IO_SELECT_COUNT, LoopResources.DEFAULT_IO_WORKER_COUNT, false, true));
    }
}
