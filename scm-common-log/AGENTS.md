# AGENTS.md - scm-common-log

## Module Purpose

Common logging model module.

Contain shared log models and structured logging contracts.

---

## Source of Truth

Use branch:

```text
features/CMNEW-119
```

Also read root `AGENTS.md` before editing this module.

---

## Allowed Responsibilities

- Keep changes local to this module's responsibility.
- Follow existing package and naming conventions.
- Use Java 21 and current project dependency versions.
- Preserve public contracts unless the change explicitly requires versioned contract evolution.
- Keep code understandable for junior developers.

---

## Forbidden Responsibilities

- Do not introduce secrets.
- Do not hardcode provider URLs, credentials or environment-specific values.
- Do not bypass SCM error handling.
- Do not leak provider-specific exceptions outside module boundaries.
- Do not introduce protocol formatting in non-gateway modules.
- Do not upgrade global framework versions from this module.

---

## Error Handling

Errors must follow the SCM flow:

```text
Module Error
    -> SCMException
    -> SCMFault
    -> caller/protocol formatter
```

Rules:

- Map local technical errors to SCM domain errors.
- Preserve correlation and diagnostic metadata.
- Do not expose sensitive data in exception messages.

---

## Observability

When adding logs or spans:

- Include correlation id where available.
- Prefer structured fields.
- Avoid large payload dumps.
- Never log password, token, client_secret or authorization_code.

---

## Testing Guidance

Add or update tests when:

- Public behavior changes.
- Error mapping changes.
- Provider interaction changes.
- Plugin execution behavior changes.
- Routing or resilience behavior changes.

Do not call real external systems in unit tests.

---

## Agent Checklist

Before completing a change in `scm-common-log`:

1. Confirm the change belongs in this module.
2. Confirm no secrets were added.
3. Confirm error handling still maps to SCM standard flow.
4. Confirm observability/correlation is preserved.
5. Confirm no unrelated module or dependency version changed.
