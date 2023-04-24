package benchmark;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import jakarta.inject.Singleton;

@Singleton
public class PropertiesPrinter implements ApplicationEventListener<ApplicationStartupEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        System.out.println("micronaut.netty.event-loops: " + event.getSource().getApplicationContext().getProperties("micronaut.netty.event-loops"));
        System.out.println("datasources.default: " + event.getSource().getApplicationContext().getProperties("datasources.default"));
        System.out.println("r2dbc.datasources.default: " + event.getSource().getApplicationContext().getProperties("r2dbc.datasources.default"));
    }
}
