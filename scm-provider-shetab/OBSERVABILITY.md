# Observability - scm-provider-shetab

## TRACE lifecycle

Shetab provider activity does not create a span. Each actual transport attempt adds exactly two ordered events to the explicit `scm.observation.scope.operation` scope owned by `operation.call`:

```text
provider.request
provider.response
```

`provider.request` is recorded after rate limiting, when the transport attempt begins. A rate-limit or request rejection before an actual transport attempt creates no pair. Once a request event is emitted, the mandatory trace-aware transport lifecycle emits exactly one response event. A failed send records its failure `provider.response` before a retry is queued; the retry receives its own pair and reliable `provider.attempt` number. The final successful send is completed only after response receipt, after-receive customizers, and ISO conversion. A unified finalization path therefore retains one failure response event for timeouts, connection failures, decoding/validation failures, and customizer failures without swallowing the original exception.

`provider.duration_ms` is present only on `provider.response`. It uses monotonic `System.nanoTime()` elapsed time and is clamped nonnegative.

Safe registered event attributes are:

```text
provider.name
provider.code
provider.type
provider.scheme
provider.operation
provider.endpoint
provider.attempt
provider.duration_ms
provider.response_code
provider.error_code
event.outcome
error.type
error.code
```

Future provider-specific request or response attributes use `ShetabProviderTraceAttributeContributor`. Every added field must be explicitly registered through its inherited `ObservationAttributeContributor` contract; the sanitizer continues to reject arbitrary fields.

`provider.endpoint` is emitted only when the actual connected endpoint for that attempt is known. Reconnect and connection-failure attempts omit the field rather than falling back to the first configured endpoint.

## Metrics

Metrics remain on the Actuator/Micrometer/Prometheus path. They are not written to TRACE files.

## Forbidden data

Never log, trace, audit, or tag raw/map/packed ISO request or response payloads, PAN, account data, CVV2, PIN or PIN blocks, MAC values, track data, credentials, tokens, Authorization headers, or unrestricted exception messages.
