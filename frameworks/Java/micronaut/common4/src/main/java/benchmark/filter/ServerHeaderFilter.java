package benchmark.filter;

import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.scheduling.annotation.Scheduled;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ServerFilter(Filter.MATCH_ALL_PATTERN)
public class ServerHeaderFilter {

    private volatile String dateHeader = newDateHeader();

    ServerHeaderFilter() {
        setDateHeader();
    }

    @Scheduled(fixedRate = "1s")
    public void setDateHeader() {
        dateHeader = newDateHeader();
    }

    private String newDateHeader() {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
    }

    @ResponseFilter
    public void addDateHeader(MutableHttpResponse<?> response) {
        response.header("Date", dateHeader);
    }

}