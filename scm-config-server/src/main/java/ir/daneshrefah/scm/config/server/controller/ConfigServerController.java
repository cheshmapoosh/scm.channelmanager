package ir.daneshrefah.scm.config.server.controller;

import ir.daneshrefah.scm.config.server.config.ConfigConfiguration;
import ir.daneshrefah.scm.config.server.service.GitService;
import ir.daneshrefah.scm.config.server.service.YamlService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
public class ConfigServerController {
    private static final String DEFAULT = "default";
    public static final String ALL = "all";
    public static final String ENC_TAG = "{enc}";
    public static final String CIPHER_TAG = "{cipher}";
    private Map<String, Set<String>> applications;
    private final EnvironmentRepository environmentRepository;
    private final YamlService yamlService;
    private final GitService gitService;
    private final TextEncryptor textEncryptor;

    public ConfigServerController(@Qualifier("searchPathCompositeEnvironmentRepository") EnvironmentRepository environmentRepository,
                                  YamlService yamlService,
                                  GitService gitService,
                                  ConfigConfiguration configConfiguration,
                                  TextEncryptor textEncryptor) {
        this.environmentRepository = environmentRepository;
        this.yamlService = yamlService;
        this.gitService = gitService;
        this.applications = configConfiguration.getApplications();
        this.textEncryptor = textEncryptor;
    }

    @GetMapping("/application")
    public Set<String> getApplications() {
        return applications.keySet();

    }

    @GetMapping("/application/{application}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public Set<String> getProfiles(@PathVariable String application) {
        return applications.get(application);
    }


    @GetMapping("/property/{application}/{profile}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Map<?, ?>> getProperty(@PathVariable String application,
                                              @PathVariable String profile) {
        Map<String, Set<String>> applications = new HashMap<>(this.applications);
        ;
        if (!StringUtils.equalsIgnoreCase(ALL, application)) {
            applications = new HashMap<>();
            applications.put(application, this.applications.get(application));
        }

        if (!StringUtils.equalsIgnoreCase(ALL, profile)) {
            List<String> keys = applications.keySet().stream().toList();
            List<Set<String>> values = applications.values().stream().toList();
            applications = IntStream.range(0, keys.size()).boxed()
                    .collect(Collectors.toMap(keys::get,
                            i -> values.get(i).stream().filter(p -> StringUtils.equals(p, profile)).collect(Collectors.toSet()),
                            (k, v) -> v,
                            HashMap::new));
        }

        applications = applications.entrySet().stream()
                .filter(entry -> CollectionUtils.size(entry.getValue()) > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> v, HashMap::new));

        Set<Environment> environments = applications.entrySet()
                .stream().flatMap(entry -> {
                    String a = entry.getKey();
                    return entry.getValue().stream().map(p -> environmentRepository.findOne(a, p, null));
                }).collect(Collectors.toSet());

        return environments.stream()
                .flatMap(environment -> environment.getPropertySources().stream())
                .collect(Collectors.toMap(propertySource -> {
                           String name = propertySource.getName();
                           name = StringUtils.removeStart(name, gitService.getGitUri());
                            name = StringUtils.removeStart(name, "/");
                            name = StringUtils.replace(name, "application.yml", DEFAULT);
                            name = StringUtils.replace(name, "application-", StringUtils.EMPTY);
                            name = StringUtils.replace(name, ".yml", StringUtils.EMPTY);
                           return name;
                        },
                        p -> p.getSource(),
                        (k, v) -> v,
                        HashMap::new));

//        return environment.getPropertySources().stream()
//                .map(propertySource -> propertySource.getSource().get(""))
//                .filter(value -> value != null)
//                .map(Object::toString)
//                .findFirst()
//                .orElse("Property not found");
    }

    @PutMapping("/property")
    public String updateProperty(@RequestParam(required = false) String application,
                                 @RequestParam(required = false) String profile,
                                 @RequestParam String key,
                                 @RequestParam String value) throws Exception {
        Path path = Paths.get(gitService.getGitBaseDir(),
                applicationName(application),
                profileName(profile));
        if (StringUtils.startsWith(value, ENC_TAG)){
            value = textEncryptor.encrypt(StringUtils.removeStart(value, ENC_TAG));
            value = StringUtils.join(CIPHER_TAG, value);
        }
        yamlService.updateProperty(path, key, value);
        gitService.commitAndPush("Updated config property " + key);
        return "Configuration updated and pushed to Git repository.";
    }

    private String applicationName(String application) {
        return StringUtils.defaultString(application);
    }

    private String profileName(String profile) {
        if (StringUtils.isEmpty(profile) || StringUtils.endsWithIgnoreCase(profile, DEFAULT)) {
            return "application.yml";
        }
        return "application-" + profile + ".yml";
    }
}
