# AGENTS.md - scm-provider-scm

## Module Purpose

Spring-managed internal SCM Resource provider.

Own the `scm:{resource.name}` Camel component, startup Resource catalog,
action validation, invocation boundary, and module-local documentation.

## Architecture Rules

- Read the repository root `AGENTS.md` before editing this module.
- Keep Resource annotations and business contracts in `scm-plugin-api`.
- Keep concrete Resource implementations with the capability or typed client they own.
- Resolve only explicitly annotated Resource Actions.
- Preserve Spring-managed proxy instances when constructing Camel Bean delegates.
- Never accept bean names, Java method names, class names, or SpEL as executable database configuration.
- Keep Resource discovery and invocation metadata immutable after startup.

## Error and Observability Rules

- Preserve existing SCM domain exceptions.
- Translate provider-boundary failures to stable SCM Resource error codes.
- Preserve correlation metadata already carried by the Camel Exchange.
- Do not record payloads or sensitive values in logs, traces, audits, metrics, or exception messages.
- Do not write metrics to files or change the standard Actuator/Micrometer flow.
