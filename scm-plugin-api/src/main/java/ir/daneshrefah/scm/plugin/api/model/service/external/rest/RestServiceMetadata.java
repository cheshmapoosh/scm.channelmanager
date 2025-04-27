package ir.daneshrefah.scm.plugin.api.model.service.external.rest;

import lombok.Data;

import java.util.List;

@Data
public class RestServiceMetadata {
    private String engine;
    private String method;
    private String pathTemplate;
    private String headersTemplate;
    private String requestTemplate;
    private List<RestResponseTemplate> responseTemplates;
}
