package benchmark;

import io.micronaut.runtime.Micronaut;

public class R2dbcApplication {

    public static void main(String[] args) {
        Micronaut.build(args).environments("common").classes(Application.class).start();
    }

}