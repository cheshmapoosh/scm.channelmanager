package ir.daneshrefah.scm.core.integration.service.scanner.spec;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
@Order(1)
@Slf4j
@RequiredArgsConstructor
public class ApplicationContextScanner {

    private static final String BASE_PACKAGE = "ir.daneshrefah.scm";
    private final List<ContextScannerModule> contextScannerModules;

    @PostConstruct
    public void load() {
        LocalDateTime started = LocalDateTime.now();
        log.info(">>> Application context scanner loading started");
        cacheClassInfo();
        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(started, end);
        log.info(">>> Application context scanner successfully scanned done on {} seconds", duration.getSeconds());
    }

    public void scanContext(ClassInfoRunner<?> classInfoRunner) {
        ScanResult scanResult = new ClassGraph()
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo()
                .acceptPackages(BASE_PACKAGE)
                .scan();
        try (scanResult) {
            classInfoRunner.execute(scanResult);
        }
    }

    public void cacheClassInfo() {
        final Map<String, ClassInfo> foundClasses = new HashMap<>();
        scanContext(scanResult -> {
            Map<String, ClassInfo> allClassesAsMap = scanResult.getAllClassesAsMap();
            Set<String> classPathName = allClassesAsMap.keySet();
            classPathName
                    .stream()
                    .filter(name -> name.toLowerCase().startsWith(BASE_PACKAGE))
                    .forEach(name -> {
                        ClassInfo classInfo = allClassesAsMap.get(name);
                        foundClasses.put(name, classInfo);
                    });
            log.info(">>> [Context Scanner] all '{}' classes has been found ", foundClasses.size());
            contextScannerModules.forEach(contextScannerModule -> contextScannerModule.register(foundClasses));
        });
    }

}
