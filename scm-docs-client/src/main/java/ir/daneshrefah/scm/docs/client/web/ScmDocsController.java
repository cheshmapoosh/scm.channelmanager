package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import ir.daneshrefah.scm.docs.client.registry.ScmDocsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("${scm.docs.base-path:/docs}")
public class ScmDocsController {

    private static final Logger log = LoggerFactory.getLogger(ScmDocsController.class);

    private final ScmDocsRegistry registry;

    private final ScmDocsHtmlRenderer htmlRenderer;

    private final ScmDocsProperties properties;

    public ScmDocsController(ScmDocsRegistry registry, ScmDocsHtmlRenderer htmlRenderer, ScmDocsProperties properties) {
        this.registry = registry;
        this.htmlRenderer = htmlRenderer;
        this.properties = properties;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String docs(Locale locale) {
        log.debug("SCM docs HTML index requested language={} basePath={}", languageFrom(locale), properties.normalizedBasePath());
        return htmlRenderer.render(
                properties.getTitle(),
                languageFrom(locale),
                properties.normalizedBasePath(),
                descriptorsWithHref()
        );
    }

    @GetMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> apiIndex() {
        List<ScmDocDescriptor> documents = descriptorsWithHref();
        log.debug("SCM docs API index requested documentCount={}", documents.size());
        return Map.of("documents", documents);
    }

    @GetMapping(path = "/api/{docId}")
    public ResponseEntity<byte[]> apiContent(@PathVariable String docId) {
        log.debug("SCM docs content requested docId={}", docId);
        return registry.findById(docId)
                .map(this::toContentResponse)
                .orElseGet(() -> {
                    log.warn("SCM docs content not found docId={}", docId);
                    return ResponseEntity.notFound().build();
                });
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(IllegalArgumentException exception) {
        log.warn("Invalid SCM docs API request reason={}", exception.getMessage());
        return Map.of("error", "Invalid documentation request");
    }

    private ResponseEntity<byte[]> toContentResponse(ScmDocContent content) {
        ScmDocDescriptor descriptor = content.descriptor();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(content.mediaType()));
        String safeFileName = safeFileName(content.fileName());
        if (hasText(safeFileName)) {
            headers.setContentDisposition(ContentDisposition.inline().filename(safeFileName).build());
        }
        log.debug("SCM docs content response created docId={} type={} category={} moduleCode={} serviceCode={} version={} mediaType={} fileName={}",
                descriptor.id(), descriptor.type(), descriptor.category(), descriptor.moduleCode(),
                descriptor.serviceCode(), descriptor.version(), content.mediaType(), safeFileName);
        return new ResponseEntity<>(content.body(), headers, HttpStatus.OK);
    }

    static String safeFileName(String fileName) {
        if (!hasText(fileName)) {
            return null;
        }
        String safeValue = fileName.trim()
                .replace('\0', '_')
                .replace('\r', '_')
                .replace('\n', '_')
                .replace('/', '_')
                .replace('\\', '_');
        while (safeValue.contains("..")) {
            safeValue = safeValue.replace("..", "_");
        }
        safeValue = safeValue.replaceAll("_+", "_");
        safeValue = trimUnsafeEdges(safeValue);
        return hasText(safeValue) ? safeValue : null;
    }

    private List<ScmDocDescriptor> descriptorsWithHref() {
        return registry.findAll().stream()
                .map(descriptor -> descriptor.withHref(hrefFor(descriptor.id())))
                .toList();
    }

    private String hrefFor(String docId) {
        return properties.normalizedBasePath() + "/api/" + urlEncode(docId);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String languageFrom(Locale locale) {
        return locale == null ? null : locale.toLanguageTag();
    }

    private static String trimUnsafeEdges(String value) {
        String trimmed = value;
        while (trimmed.startsWith("_") || trimmed.startsWith(".")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("_") || trimmed.endsWith(".")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
