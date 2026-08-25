package ir.daneshrefah.scm.provider.scm.registry;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationProvider;
import ir.daneshrefah.scm.common.provider.runtime.ProviderRuntimeLifecycle;
import ir.daneshrefah.scm.plugin.api.resource.ResourceAction;
import ir.daneshrefah.scm.plugin.api.resource.ScmResource;
import ir.daneshrefah.scm.provider.scm.camel.ScmComponent;
import ir.daneshrefah.scm.provider.scm.exception.ScmResourceProviderException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Service;
import org.apache.camel.component.bean.BeanProcessor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Immutable startup catalog of explicitly registered SCM Resources and Actions.
 */
public final class ScmResourceRegistry implements ProviderRuntimeLifecycle {

    private static final String URI_PREFIX = ScmComponent.SCHEME + ":";

    private final Map<String, ScmResourceDescriptor> resources;
    private final Map<ScmResourceActionKey, ScmResourceActionDescriptor> actions;

    public ScmResourceRegistry(
            ApplicationContext applicationContext,
            CamelContext camelContext,
            ObjectMapper objectMapper
    ) {
        DiscoveryResult result = discover(applicationContext, camelContext, objectMapper);
        this.resources = immutableCopy(result.resources());
        this.actions = immutableCopy(result.actions());
    }

    public Map<String, ScmResourceDescriptor> resources() {
        return resources;
    }

    public ScmResourceDescriptor requireResource(String resourceName) {
        ScmResourceDescriptor descriptor = resources.get(resourceName);
        if (descriptor == null) {
            throw ScmResourceProviderException.resourceNotFound();
        }
        return descriptor;
    }

    public ScmResourceActionDescriptor requireAction(String resourceName, String actionName) {
        requireResource(resourceName);
        ScmResourceActionDescriptor descriptor = actions.get(new ScmResourceActionKey(resourceName, actionName));
        if (descriptor == null) {
            throw ScmResourceProviderException.actionNotFound();
        }
        return descriptor;
    }

    public void validateOperation(Operation operation) {
        Objects.requireNonNull(operation, "operation");
        OperationProvider provider = Objects.requireNonNull(
                operation.getProvider(),
                "SCM Resource Operation provider is required"
        );
        String resourceName = resourceNameFromUri(provider.getUri(), operation.getName());
        String actionName = ScmResourceNames.requireActionName(
                operation.getPath(),
                "SCM Resource Operation path for operation '" + operation.getName() + "'"
        );
        requireRegisteredAction(resourceName, actionName, operation.getName());
    }

    @Override
    public boolean supports(String scheme) {
        return ScmComponent.SCHEME.equalsIgnoreCase(scheme);
    }

    @Override
    public void registerEffectiveUsage(EffectiveProviderUsage usage) {
        Objects.requireNonNull(usage, "usage");
        if (!supports(usage.scheme())) {
            return;
        }
        String resourceName = resourceNameFromUri(usage.providerUri(), usage.operationName());
        String actionName = ScmResourceNames.requireActionName(
                usage.operationPath(),
                "SCM Resource Operation path for operation '" + usage.operationName() + "'"
        );
        requireRegisteredAction(resourceName, actionName, usage.operationName());
    }

    private DiscoveryResult discover(
            ApplicationContext applicationContext,
            CamelContext camelContext,
            ObjectMapper objectMapper
    ) {
        Map<String, Object> candidates = applicationContext.getBeansWithAnnotation(ScmResource.class);
        List<Map.Entry<String, Object>> orderedCandidates = candidates.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();

        Map<String, ScmResourceDescriptor> discoveredResources = new LinkedHashMap<>();
        Map<ScmResourceActionKey, ScmResourceActionDescriptor> discoveredActions = new LinkedHashMap<>();
        for (Map.Entry<String, Object> candidate : orderedCandidates) {
            ScmResourceDescriptor resource = describeResource(
                    applicationContext,
                    camelContext,
                    objectMapper,
                    candidate.getKey(),
                    candidate.getValue()
            );
            ScmResourceDescriptor duplicate = discoveredResources.putIfAbsent(resource.resourceName(), resource);
            if (duplicate != null) {
                throw new IllegalStateException("Duplicate SCM Resource name '" + resource.resourceName()
                        + "' on Spring beans '" + duplicate.springBeanName()
                        + "' and '" + resource.springBeanName() + "'");
            }
            for (ScmResourceActionDescriptor action : resource.actions().values()) {
                ScmResourceActionKey key = new ScmResourceActionKey(action.resourceName(), action.actionName());
                ScmResourceActionDescriptor duplicateAction = discoveredActions.putIfAbsent(key, action);
                if (duplicateAction != null) {
                    throw new IllegalStateException("Duplicate SCM Resource Action '" + action.resourceName()
                            + ":" + action.actionName() + "'");
                }
            }
        }
        return new DiscoveryResult(discoveredResources, discoveredActions);
    }

    private ScmResourceDescriptor describeResource(
            ApplicationContext applicationContext,
            CamelContext camelContext,
            ObjectMapper objectMapper,
            String beanName,
            Object springBean
    ) {
        Class<?> targetType = AopUtils.getTargetClass(springBean);
        ScmResource annotation = applicationContext.findAnnotationOnBean(beanName, ScmResource.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(targetType, ScmResource.class);
        }
        if (annotation == null) {
            throw new IllegalStateException("Unable to resolve @ScmResource on Spring bean '" + beanName + "'");
        }

        String resourceName = ScmResourceNames.requireResourceName(
                annotation.value(),
                "@ScmResource value on Spring bean '" + beanName + "'"
        );
        if (!resourceName.equals(beanName)) {
            throw new IllegalStateException("SCM Resource name '" + resourceName
                    + "' must equal its Spring bean name, but the bean name is '" + beanName + "'");
        }
        if (!applicationContext.isSingleton(beanName)) {
            throw new IllegalStateException("SCM Resource bean '" + beanName + "' must be a singleton");
        }

        Map<String, ScmResourceActionDescriptor> resourceActions = describeActions(
                camelContext,
                objectMapper,
                resourceName,
                beanName,
                targetType,
                springBean
        );
        if (resourceActions.isEmpty()) {
            throw new IllegalStateException("SCM Resource bean '" + beanName
                    + "' does not declare a valid @ResourceAction method");
        }
        return new ScmResourceDescriptor(resourceName, beanName, targetType, resourceActions);
    }

    private Map<String, ScmResourceActionDescriptor> describeActions(
            CamelContext camelContext,
            ObjectMapper objectMapper,
            String resourceName,
            String beanName,
            Class<?> targetType,
            Object springBean
    ) {
        Map<Method, String> selected = MethodIntrospector.selectMethods(
                targetType,
                (MethodIntrospector.MetadataLookup<String>)
                        method -> findActionName(targetType, method)
        );
        Map<Method, String> normalized = new LinkedHashMap<>();
        selected.forEach((method, actionName) -> {
            Method bridged = BridgeMethodResolver.findBridgedMethod(method);
            String existing = normalized.putIfAbsent(bridged, actionName);
            if (existing != null && !existing.equals(actionName)) {
                throw new IllegalStateException("Conflicting @ResourceAction names on method '"
                        + bridged.getName() + "' of SCM Resource '" + resourceName + "'");
            }
        });

        Map<String, ScmResourceActionDescriptor> descriptors = new LinkedHashMap<>();
        normalized.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toGenericString()))
                .forEach(entry -> {
                    Method method = entry.getKey();
                    String actionName = ScmResourceNames.requireActionName(
                            entry.getValue(),
                            "@ResourceAction value on " + targetType.getName() + "#" + method.getName()
                    );
                    JavaType inputType = method.getParameterCount() == 0
                            ? null
                            : resolveActionType(
                                    objectMapper,
                                    targetType,
                                    method.getGenericParameterTypes()[0],
                                    resourceName,
                                    method,
                                    "input"
                            );
                    JavaType outputType = resolveActionType(
                            objectMapper,
                            targetType,
                            method.getGenericReturnType(),
                            resourceName,
                            method,
                            "output"
                    );
                    validateMethod(targetType, springBean, method, resourceName, inputType, outputType);
                    BeanProcessor delegate = createDelegate(camelContext, springBean, method, resourceName, actionName);
                    ScmResourceActionDescriptor descriptor = new ScmResourceActionDescriptor(
                            resourceName,
                            beanName,
                            actionName,
                            method.getName(),
                            inputType,
                            outputType,
                            delegate
                    );
                    ScmResourceActionDescriptor duplicate = descriptors.putIfAbsent(actionName, descriptor);
                    if (duplicate != null) {
                        throw new IllegalStateException("Duplicate @ResourceAction value '" + actionName
                                + "' on SCM Resource '" + resourceName + "'");
                    }
                });
        return descriptors;
    }

    private String findActionName(Class<?> targetType, Method candidate) {
        Method method = BridgeMethodResolver.findBridgedMethod(candidate);
        Set<String> names = new LinkedHashSet<>();
        addActionName(names, AnnotatedElementUtils.findMergedAnnotation(method, ResourceAction.class));
        for (Class<?> resourceInterface : ClassUtils.getAllInterfacesForClassAsSet(targetType)) {
            for (Method interfaceMethod : resourceInterface.getMethods()) {
                if (mapsToActionMethod(targetType, interfaceMethod, method)) {
                    addActionName(
                            names,
                            AnnotatedElementUtils.findMergedAnnotation(interfaceMethod, ResourceAction.class)
                    );
                }
            }
        }
        if (names.size() > 1) {
            throw new IllegalStateException("Conflicting @ResourceAction values on "
                    + targetType.getName() + "#" + method.getName());
        }
        return names.stream().findFirst().orElse(null);
    }

    private boolean mapsToActionMethod(Class<?> targetType, Method interfaceMethod, Method actionMethod) {
        if (!interfaceMethod.getName().equals(actionMethod.getName())
                || interfaceMethod.getParameterCount() != actionMethod.getParameterCount()) {
            return false;
        }
        Method mostSpecific = BridgeMethodResolver.getMostSpecificMethod(interfaceMethod, targetType);
        return BridgeMethodResolver.findBridgedMethod(mostSpecific).equals(actionMethod);
    }

    private void addActionName(Set<String> names, ResourceAction annotation) {
        if (annotation != null) {
            names.add(annotation.value());
        }
    }

    private void validateMethod(
            Class<?> targetType,
            Object springBean,
            Method method,
            String resourceName,
            JavaType inputType,
            JavaType outputType
    ) {
        if (!Modifier.isPublic(method.getModifiers())) {
            throw invalidMethod(resourceName, method, "must be public");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            throw invalidMethod(resourceName, method, "must not be static");
        }
        if (method.isVarArgs()) {
            throw invalidMethod(resourceName, method, "must not use varargs");
        }
        if (method.getParameterCount() > 1) {
            throw invalidMethod(resourceName, method, "must declare zero or one input parameter");
        }
        if (method.getReturnType() == Void.TYPE) {
            throw invalidMethod(resourceName, method, "must return a value");
        }
        if (hasOverload(targetType, method.getName())) {
            throw invalidMethod(resourceName, method, "must not be overloaded");
        }
        if (inputType != null && isInfrastructureType(inputType)) {
            throw invalidMethod(resourceName, method, "must not accept provider infrastructure");
        }
        if (isInfrastructureType(outputType)) {
            throw invalidMethod(resourceName, method, "must not return provider infrastructure");
        }
        if (!isInvocableThroughProxy(targetType, springBean, method)) {
            throw invalidMethod(resourceName, method, "is not invocable through the Spring proxy");
        }
    }

    private boolean isInvocableThroughProxy(Class<?> targetType, Object springBean, Method actionMethod) {
        try {
            MethodIntrospector.selectInvocableMethod(actionMethod, springBean.getClass());
            return true;
        } catch (IllegalStateException ignored) {
            for (Class<?> resourceInterface : ClassUtils.getAllInterfacesForClassAsSet(targetType)) {
                for (Method interfaceMethod : resourceInterface.getMethods()) {
                    if (!mapsToActionMethod(targetType, interfaceMethod, actionMethod)) {
                        continue;
                    }
                    try {
                        MethodIntrospector.selectInvocableMethod(interfaceMethod, springBean.getClass());
                        return true;
                    } catch (IllegalStateException ignoredInterfaceMethod) {
                        // Try the next originating interface method.
                    }
                }
            }
            return false;
        }
    }

    private JavaType resolveActionType(
            ObjectMapper objectMapper,
            Class<?> targetType,
            Type declaredType,
            String resourceName,
            Method method,
            String role
    ) {
        Map<TypeVariable, Type> typeVariables = GenericTypeResolver.getTypeVariableMap(targetType);
        if (containsUnsupportedGeneric(declaredType, typeVariables, new LinkedHashSet<>())) {
            throw invalidMethod(resourceName, method,
                    "has an unresolved or wildcard " + role + " generic type");
        }

        Type resolvedType = GenericTypeResolver.resolveType(declaredType, targetType);
        if (containsUnsupportedGeneric(resolvedType, Map.of(), new LinkedHashSet<>())) {
            throw invalidMethod(resourceName, method,
                    "has an unresolved or wildcard " + role + " generic type");
        }
        if (containsRawGeneric(resolvedType)) {
            throw invalidMethod(resourceName, method,
                    "must not use a raw generic " + role + " type");
        }

        try {
            return objectMapper.getTypeFactory().constructType(resolvedType);
        } catch (IllegalArgumentException exception) {
            throw invalidMethod(resourceName, method,
                    "has an unsupported " + role + " type");
        }
    }

    private boolean containsUnsupportedGeneric(
            Type type,
            Map<TypeVariable, Type> typeVariables,
            Set<TypeVariable> visiting
    ) {
        if (type instanceof TypeVariable<?> variable) {
            if (!typeVariables.containsKey(variable) || !visiting.add(variable)) {
                return true;
            }
            boolean unsupported = containsUnsupportedGeneric(typeVariables.get(variable), typeVariables, visiting);
            visiting.remove(variable);
            return unsupported;
        }
        if (type instanceof WildcardType) {
            return true;
        }
        if (type instanceof GenericArrayType arrayType) {
            return containsUnsupportedGeneric(arrayType.getGenericComponentType(), typeVariables, visiting);
        }
        if (type instanceof ParameterizedType parameterizedType) {
            if (ownerTypeContributesGenerics(parameterizedType)
                    && containsUnsupportedGeneric(parameterizedType.getOwnerType(), typeVariables, visiting)) {
                return true;
            }
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                    .anyMatch(argument -> containsUnsupportedGeneric(argument, typeVariables, visiting));
        }
        return false;
    }

    private boolean containsRawGeneric(Type type) {
        if (type instanceof Class<?> typeClass) {
            return typeClass.getTypeParameters().length > 0;
        }
        if (type instanceof GenericArrayType arrayType) {
            return containsRawGeneric(arrayType.getGenericComponentType());
        }
        if (type instanceof ParameterizedType parameterizedType) {
            if (ownerTypeContributesGenerics(parameterizedType)
                    && containsRawGeneric(parameterizedType.getOwnerType())) {
                return true;
            }
            return Arrays.stream(parameterizedType.getActualTypeArguments())
                    .anyMatch(this::containsRawGeneric);
        }
        return false;
    }

    private boolean ownerTypeContributesGenerics(ParameterizedType type) {
        if (type.getOwnerType() == null) {
            return false;
        }
        return !(type.getRawType() instanceof Class<?> rawClass)
                || !Modifier.isStatic(rawClass.getModifiers());
    }

    private boolean hasOverload(Class<?> targetType, String methodName) {
        return Stream.concat(
                        Arrays.stream(ReflectionUtils.getAllDeclaredMethods(targetType)),
                        Arrays.stream(targetType.getMethods())
                )
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> !method.isBridge() && !method.isSynthetic())
                .map(method -> List.of(method.getParameterTypes()))
                .distinct()
                .limit(2)
                .count() > 1;
    }

    private boolean isInfrastructureType(Class<?> type) {
        return Exchange.class.isAssignableFrom(type)
                || org.apache.camel.Message.class.isAssignableFrom(type)
                || CamelContext.class.isAssignableFrom(type)
                || Service.class.isAssignableFrom(type)
                || ApplicationContext.class.isAssignableFrom(type)
                || BeanFactory.class.isAssignableFrom(type)
                || AccessibleObject.class.isAssignableFrom(type)
                || type == Class.class
                || type.getName().startsWith("ir.daneshrefah.scm.provider.scm.");
    }

    private boolean isInfrastructureType(JavaType type) {
        if (isInfrastructureType(type.getRawClass())) {
            return true;
        }
        for (int index = 0; index < type.containedTypeCount(); index++) {
            JavaType nestedType = type.containedType(index);
            if (nestedType != null && isInfrastructureType(nestedType)) {
                return true;
            }
        }
        return false;
    }

    private BeanProcessor createDelegate(
            CamelContext camelContext,
            Object springBean,
            Method method,
            String resourceName,
            String actionName
    ) {
        try {
            BeanProcessor delegate = new BeanProcessor(springBean, camelContext);
            delegate.setMethod(method.getName());
            camelContext.addService(delegate);
            return delegate;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build Camel Bean delegate for SCM Resource Action '"
                    + resourceName + ":" + actionName + "'", exception);
        }
    }

    private IllegalStateException invalidMethod(String resourceName, Method method, String reason) {
        return new IllegalStateException("SCM Resource Action method '" + resourceName + ":"
                + method.getName() + "' " + reason);
    }

    private void requireRegisteredAction(String resourceName, String actionName, String operationName) {
        ScmResourceDescriptor resource = resources.get(resourceName);
        if (resource == null) {
            throw new IllegalStateException("Active SCM Operation '" + operationName
                    + "' references missing Resource '" + resourceName + "'");
        }
        if (!actions.containsKey(new ScmResourceActionKey(resourceName, actionName))) {
            throw new IllegalStateException("Active SCM Operation '" + operationName
                    + "' references missing Action '" + resourceName + ":" + actionName + "'");
        }
    }

    private String resourceNameFromUri(String uri, String operationName) {
        if (uri == null || !uri.startsWith(URI_PREFIX)) {
            throw new IllegalStateException("Active SCM Operation '" + operationName
                    + "' must use URI contract " + URI_PREFIX + "{resource.name}");
        }
        String remaining = uri.substring(URI_PREFIX.length());
        return ScmResourceNames.requireResourceName(
                remaining,
                "SCM Resource name in provider URI for operation '" + operationName + "'"
        );
    }

    private static <K, V> Map<K, V> immutableCopy(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private record DiscoveryResult(
            Map<String, ScmResourceDescriptor> resources,
            Map<ScmResourceActionKey, ScmResourceActionDescriptor> actions
    ) {
    }
}
