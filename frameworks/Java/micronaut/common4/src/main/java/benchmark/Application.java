package benchmark;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static void main(String[] args) {
        System.setProperty("reactor.bufferSize.small", "512");
        Micronaut.build(args).environments("common").classes(Application.class).start();
    }

}