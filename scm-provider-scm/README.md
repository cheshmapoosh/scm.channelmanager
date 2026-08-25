# SCM Internal Resource Provider

## 1. Purpose and non-goals

`scm-provider-scm` exposes explicitly registered, Spring-managed internal SCM
Resources to `PROVIDER` Operations through the Camel scheme `scm`.

The module gives an Operation and a Java Plugin two entry paths to the same
Spring bean:

```text
Operation -> scm:{resource} -> Resource Action registry -> cached Camel Bean delegate -> Resource bean
Plugin    -> constructor-injected Resource interface -> direct Java call -> same Resource bean
```

This module does not provide external-provider URL joining, base-path joining,
or a new service routing policy. It does not replace existing REST, NAB,
Shetab, Task, `BEAN`, or Java operation behavior. It also does not create a
second business implementation for Operation invocation.

## 2. Ownership and dependencies

- `scm-plugin-api` owns the stable `@ScmResource` and `@ResourceAction`
  contracts. Typed Resource interfaces may also live there when multiple
  Plugins need them.
- `scm-provider-scm` owns the Camel component, immutable Resource registry,
  startup validation, input adaptation, Camel Bean delegates, and provider
  boundary errors.
- A concrete Resource belongs to the module that owns its capability or typed
  client. It may inject lower-level services or clients, but must not depend on
  Plugin handlers, route builders, or gateway protocol formatting.
- `scm-web` includes this provider so its auto-configuration, registry, and
  `scm` component are present in the ChannelManager runtime.

The implementation uses the project-managed Camel version and Camel Bean
support. It uses Spring AOP/introspection APIs so delegates retain the actual
Spring proxy instead of invoking a separately constructed target object.

## 3. Invocation flows

### Operation path

```text
Operation(type = PROVIDER)
  -> OperationProvider(uri = scm:uaa)
  -> operation.path = otp.verify
  -> existing ProviderOperationTypeHandler
  -> scm:uaa endpoint
  -> immutable (uaa, otp.verify) descriptor
  -> cached Camel BeanProcessor for verifyOtp
  -> Spring bean named uaa
```

The existing Operation object is stored on the Camel Exchange by the Operation
route. `ScmProducer` reads its `path` as the logical Action. No `toD()`, dynamic
endpoint creation, or per-request Java method resolution is used.

The `scm` scheme has a focused payload hook in the existing provider handler so
typed bodies are not converted through the legacy generic `Map` path. An
already compatible non-generic body remains unchanged. For parameterized
collections, arrays, and maps, compatible nested runtime values are also
preserved. JSON text, a `JsonNode`, an untyped `Map` or `List`, or a body whose
nested types cannot be proven compatible is converted once with the complete
startup-resolved Jackson `JavaType` before Camel Bean binding invokes the
action.

### Plugin path

```text
Plugin bean
  -> constructor-injected typed Resource interface
  -> ordinary Java method call
  -> same Spring bean
```

Plugin calls do not use Camel, registry lookup, or reflection. Spring AOP
proxies for transactions, security, and observation remain effective on both
paths because the registry caches the Spring bean instance, not a new target.

## 4. URI contract

The only endpoint form is:

```text
scm:{resource.name}
```

Examples:

```text
scm:uaa
scm:core
scm:operation-management
```

Endpoint query parameters, extra paths, class names, and method names are not
accepted. The `scm` scheme is reserved for internal SCM Resources registered
with `@ScmResource`.

## 5. Provider and Operation configuration

New Resource calls use `OperationType.PROVIDER`, not `OperationType.BEAN`.
Existing `BEAN` Operations remain unchanged for backward compatibility.

```text
OperationProvider:
  name   = UAA_RESOURCE
  uri    = scm:uaa
  active = true
```

```text
Operation:
  name     = VERIFY_OTP
  type     = PROVIDER
  provider = UAA_RESOURCE
  path     = otp.verify
  active   = true
```

`OperationProvider.name` is provider identity and may be descriptive.
`OperationProvider.uri` selects the Resource. `Operation.path` selects only a
logical annotated Action in that Resource.

## 6. Resource contracts

`@ScmResource` is a composed Spring stereotype. Its value aliases
`@Component.value`, so the class below is registered as the Spring bean named
`uaa`; no additional `@Component` or `@Service` is needed.

```java
public interface UaaResource {
    OtpVerifyResult verifyOtp(OtpVerifyCommand command);
}
```

```java
@ScmResource("uaa")
public class DefaultUaaResource implements UaaResource {

    private final UaaOtpClient otpClient;

    public DefaultUaaResource(UaaOtpClient otpClient) {
        this.otpClient = otpClient;
    }

    @Override
    @ResourceAction("otp.verify")
    public OtpVerifyResult verifyOtp(OtpVerifyCommand command) {
        return otpClient.verify(command);
    }
}
```

`@ResourceAction` may be placed on the public implementation method or its
typed interface method. Generic and inherited interfaces are supported. For
example, the annotation below is associated at startup with
`ConcreteResource.execute(Command)` even though the interface method is erased
to `execute(Object)` in bytecode:

```java
public interface Resource<I, O> {
    @ResourceAction("execute")
    O execute(I input);
}

@ScmResource("command")
public class ConcreteResource implements Resource<Command, Result> {
    @Override
    public Result execute(Command input) {
        return commandService.execute(input);
    }
}
```

Spring bridge-method and generic type-resolution APIs map the originating
interface method to the concrete method. If implementation and interface
annotations specify different Action names, startup fails. Only methods
carrying this annotation through the implementation or a mapped interface
enter the Operation registry. Other public methods on the bean are inaccessible
through `scm:`.

## 7. Naming rules

One canonical Resource name is used for all four locations:

- `@ScmResource` value
- Spring bean name
- Resource registry key
- Camel endpoint remaining path

Resource names must match:

```text
[a-z][a-z0-9-]*
```

Action names are stable identifiers independent of Java method names and must
match:

```text
[a-z][a-z0-9-]*(\.[a-z][a-z0-9-]*)*
```

Examples are `otp.verify`, `person.find`, and `service.list`. Renaming a Java
method does not change database configuration when its logical Action value is
kept stable.

## 8. Supported method signatures

The first version supports exactly:

```java
Output action(Input input)
Output action()
```

The input and output may be concrete parameterized types, including:

```java
Result action(List<OtpCommand> input)
Result action(Map<String, OtpCommand> input)
Result action(Envelope<OtpCommand> input)
Result action(Envelope<List<OtpCommand>> input)
```

All nested type arguments must resolve to concrete types for the Resource
implementation. Raw generic signatures such as `List` or `Envelope`, wildcard
signatures such as `List<?>` or `List<? extends Command>`, and unresolved class
or method type variables are deliberately unsupported and fail startup. A type
variable supplied by a concrete implementation, such as `Resource<Command,
Result>`, is resolved and supported. Supported converted inputs must also follow
the application's normal Jackson data-binding conventions.

An Action method must be public, non-static, non-varargs, non-overloaded, and
return a non-`void` value. It may accept zero or one parameter. Resource methods
must not accept or return Camel `Exchange`/`Message`/`CamelContext`, Spring
`ApplicationContext`/`BeanFactory`, reflection objects, or provider
infrastructure types.

The no-argument form is available for actions that genuinely require no input.
It does not make Exchange headers or infrastructure implicitly available to the
Resource business interface.

## 9. Startup discovery and validation

At application startup the registry:

1. asks Spring for beans explicitly marked with `@ScmResource`;
2. verifies that each bean is a singleton and its annotation value equals its
   Spring bean name;
3. obtains the target class with `AopUtils.getTargetClass` while retaining the
   original proxied bean instance;
4. resolves merged `@ResourceAction` metadata with Spring method
   introspection, including generic, inherited, and parent-interface methods;
5. maps bridge methods to their concrete implementation methods while retaining
   the actual Spring proxy as the invocation target;
6. resolves and caches complete input/output `JavaType` metadata, rejecting raw,
   wildcard, unresolved, or ambiguous generic declarations;
7. validates signatures, names, overloads, and proxy invocability;
8. builds immutable Resource and `(resourceName, actionName)` maps; and
9. creates and caches one Camel Bean invocation delegate per Action.

Startup fails for invalid or blank names, non-singleton Resources, duplicate
Resources or Actions, Resources without valid Actions, non-public/static/
varargs/overloaded methods, unsupported or unresolved generic signatures,
conflicting implementation/interface annotations, or actions that cannot be
called through the Spring proxy.

During existing effective provider runtime registration, an active `scm`
Operation is also checked against both the Resource and Action registry. A
missing `scm:{resource}` or `operation.path` therefore fails route startup
instead of waiting for a request.

## 10. Performance and caching model

- Resource annotations are scanned once when the registry bean is built.
- Method metadata, bridge/interface mappings, complete generic types, and Java
  method names are resolved once.
- Resource beans must be Spring singletons.
- Registry maps are immutable after construction.
- A Camel `BeanProcessor` holding the Spring proxy and fixed Java method name is
  cached per Action and managed by the Camel context.
- Camel endpoints and producers use normal Camel singleton caching; none are
  created per request.
- No request calls `ApplicationContext.getBean`, scans annotations, resolves a
  Java method, evaluates SpEL, calls application `Method.invoke`, or uses
  `toD()`.
- Compatible typed bodies are preserved. Parameterized collection/map/array
  contents are checked without annotation scanning; when compatibility cannot
  be proven, required conversion happens once with the cached `JavaType`.

Camel may perform its own cached Bean introspection and binding internally.
That is the supported invocation mechanism for the Operation path.

## 11. Plugin constructor injection

```java
@Component
public class OtpValidatorPlugin implements PluginHandler {

    private final UaaResource uaaResource;

    public OtpValidatorPlugin(UaaResource uaaResource) {
        this.uaaResource = uaaResource;
    }

    public OtpVerifyResult validate(OtpVerifyCommand command) {
        return uaaResource.verifyOtp(command);
    }
}
```

This is an ordinary direct Java call. The Plugin does not construct a Camel URI
and does not know the logical Action name.

## 12. Multiple Resource instances and qualifiers

When more than one Resource implements the same Java interface, inject the
canonical Resource bean explicitly:

```java
public OtpValidatorPlugin(@Qualifier("uaa") UaaResource uaaResource) {
    this.uaaResource = uaaResource;
}
```

Each instance needs a distinct valid `@ScmResource` value and exposes only its
own annotated Actions. The qualifier chooses Java injection only; it does not
alter the `scm:{resource}` contract.

## 13. Error mapping

Existing `ScmException` failures raised by a Resource or its client are
preserved and continue through the existing resolver chain. Only failures
created at this provider boundary use `ScmResourceProviderException`.

`scm-provider-scm` registers an exact-type exception resolver. It creates the
SCM `Error` directly, before the generic database-backed resolver can replace
the per-instance code with a class mapping. The four codes therefore remain
distinct even when there are no database error mappings:

| Condition | `Error.errorCode` | `Error.status` |
| --- | --- | --- |
| Resource missing | `SCM_RESOURCE_NOT_FOUND` | `SC_NOT_FOUND` |
| Action missing | `SCM_RESOURCE_ACTION_NOT_FOUND` | `SC_NOT_FOUND` |
| Action input invalid | `SCM_RESOURCE_INVALID_INPUT` | `SC_ERROR_VALIDATION` |
| Technical invocation failure | `SCM_RESOURCE_INVOCATION_FAILED` | `SC_ERROR_SYSTEM` |

The standard flow remains:

```text
Resource/client error -> ScmException -> ScmFault -> caller/protocol formatter
```

All statuses are deterministic non-success statuses. Provider errors use fixed,
locale-neutral safe messages; the resolver does not copy request values or
lower-level Spring, reflection, Camel, or client exception messages. Existing
correlation properties stay on the same Exchange and the normal global handler
copies the resolved `Error` and status into `ScmFault`.

## 14. Observability and sensitive data

The Operation route remains responsible for the existing Operation span and
Operation call/duration/fault metrics. This provider does not add a second
counter or file-based metric path, avoiding double-counting and preserving the
Actuator -> Micrometer -> Prometheus flow.

If provider-specific observations are added later, low-cardinality fields are
limited to provider scheme, validated Resource name, validated Action name,
outcome, and stable error code. Raw URIs, Java classes, payload values,
usernames, credentials, tokens, OTPs, authorization headers, and other
sensitive or high-cardinality data must never be trace attributes, metric tags,
logs, audits, or exception messages. Camel Bean trace logging must remain
disabled in environments handling sensitive Resource inputs because upstream
Camel internals can render method arguments at TRACE level.

## 15. Security restrictions

The database cannot expose arbitrary Spring beans or methods:

- only beans explicitly marked with `@ScmResource` are registered;
- `@ScmResource` value must be the canonical Spring bean name;
- only methods explicitly marked with `@ResourceAction` are registered;
- the database stores logical Resource and Action names, never Java method
  names or class names;
- no `bean:` URI, class name, method expression, Simple expression, or SpEL is
  accepted from database configuration;
- endpoint options and arbitrary remaining paths are rejected; and
- the Camel Bean delegate is fixed during startup and cannot be changed by a
  request.

Resource implementations must also avoid logging or embedding sensitive input
in exception messages.

## 16. Troubleshooting

### Missing Resource

Confirm that the owning module is on the `scm-web` runtime classpath, the class
uses `@ScmResource`, the value matches the naming expression, the bean is a
singleton, and `OperationProvider.uri` uses the exact same name.

### Missing Action

Confirm that `Operation.path` exactly matches `@ResourceAction.value`, the
method is public, and the annotation is on the implementation or matching typed
interface method. For a generic or parent interface, confirm that its type
arguments resolve to the concrete implementation method. A public unannotated
method is intentionally invisible.

### Duplicate Resource or Action

Give every Resource a unique canonical bean name and every Action within a
Resource a unique logical name. Bean overriding must not be used to hide a
duplicate Resource.

### Invalid signature or overload

Reduce the method to one supported signature, remove varargs and overloads,
return a non-`void` business type, and remove framework/infrastructure
parameters. For JDK proxies, expose the method through the injected Resource
interface. Replace raw generic types, wildcards, and unresolved type variables
with concrete parameterized types. Conflicting Action values on an
implementation and any mapped interface must be made identical.

### Inactive provider

Both the Operation and `OperationProvider` must be active. The existing core
provider lifecycle rejects inactive providers before the `scm` component is
invoked.

## 17. UAA deployment boundary example

The UAA Resource bean for ChannelManager belongs in a module loaded by
`scm-web`. Its implementation injects a typed HTTP client for the separately
deployed `scm-uaa` process:

```text
scm-web process
  -> @ScmResource("uaa") bean
  -> typed UAA HTTP client
  -> scm-uaa process
```

It must not inject server-side repositories, services, controllers, or other
beans from the separate `scm-uaa` process. The Resource is a local
ChannelManager adapter around a typed remote client. This module deliberately
does not create that concrete UAA Resource until the owning client/capability
module defines the safe commands, results, and actions.
