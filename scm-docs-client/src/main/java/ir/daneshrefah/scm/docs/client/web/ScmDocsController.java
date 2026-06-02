package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.autoconfigure.ScmDocsProperties;
import ir.daneshrefah.scm.docs.client.model.ScmDocContent;
import ir.daneshrefah.scm.docs.client.registry.ScmDocsRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${scm.docs.base-path:/docs}")
public class ScmDocsController {

    private final ScmDocsRegistry registry;

    private final ScmDocsHtmlRenderer htmlRenderer;

    private final ScmDocsProperties properties;

    public ScmDocsController(ScmDocsRegistry registry, ScmDocsHtmlRenderer htmlRenderer, ScmDocsProperties properties) {
        this.registry = registry;
        this.htmlRenderer = htmlRenderer;
        this.properties = properties;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String docs() {
        return htmlRenderer.render(properties.getTitle(), properties.normalizedBasePath(), registry.findAll());
    }

    @GetMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> apiIndex() {
        return Map.of("documents", registry.findAll());
    }

    @GetMapping(path = "/api/{docId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ScmDocContent> apiContent(@PathVariable String docId) {
        return registry.findById(docId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}
