package ir.daneshrefah.scm.web.observation.propagation;

import ir.daneshrefah.scm.observation.TraceContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ScmTracePropagationInterceptor implements ClientHttpRequestInterceptor {
    private static final List<String> REMOVED_PROPAGATION_HEADERS = List.of(
            ScmTraceParentWriter.TRACEPARENT,
            "tracestate",
            "X-Correlation-ID",
            "X-Correlation-Id",
            "X-SCM-Correlation-ID",
            "X-SCM-Trace-ID",
            "X-SCM-Span-ID",
            "X-SCM-Parent-Span-ID"
    );

    private final ScmTraceParentWriter traceParentWriter;

    public ScmTracePropagationInterceptor(ScmTraceParentWriter traceParentWriter) {
        this.traceParentWriter = traceParentWriter;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        HttpHeaders headers = request.getHeaders();
        removePropagationHeaders(headers);
        traceParentWriter.write(headers, TraceContextHolder.current());
        return execution.execute(request, body);
    }

    private void removePropagationHeaders(HttpHeaders headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        List<String> keysToRemove = new ArrayList<>();
        for (String key : headers.keySet()) {
            for (String removedHeader : REMOVED_PROPAGATION_HEADERS) {
                if (removedHeader.equalsIgnoreCase(key)) {
                    keysToRemove.add(key);
                    break;
                }
            }
        }
        keysToRemove.forEach(headers::remove);
    }
}
