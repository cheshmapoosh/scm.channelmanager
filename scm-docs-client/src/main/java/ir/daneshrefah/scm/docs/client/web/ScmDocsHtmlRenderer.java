package ir.daneshrefah.scm.docs.client.web;

import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ScmDocsHtmlRenderer {

    public String render(Map<String, String> title, String language, String basePath, List<ScmDocDescriptor> documents) {
        Map<String, String> pageTitleValues = ScmDocDescriptor.normalizeMap(title, Map.of("en", "SCM Documentation"));
        String pageTitle = ScmDocDescriptor.localizedValue(pageTitleValues, language, "SCM Documentation");
        String normalizedBasePath = normalizeBasePath(basePath);
        StringBuilder html = new StringBuilder(2048);
        html.append("""
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>""").append(escapeHtml(pageTitle)).append("""
</title>
                  <style>
                    body { margin: 0; font-family: Arial, sans-serif; color: #1f2933; background: #f7f9fb; }
                    main { max-width: 960px; margin: 0 auto; padding: 32px 20px; }
                    h1 { margin: 0 0 20px; font-size: 28px; font-weight: 700; }
                    table { width: 100%; border-collapse: collapse; background: #ffffff; border: 1px solid #d9e2ec; }
                    th, td { padding: 12px 14px; border-bottom: 1px solid #d9e2ec; text-align: left; vertical-align: top; }
                    th { font-size: 12px; letter-spacing: 0; text-transform: uppercase; color: #52616b; background: #edf2f7; }
                    a { color: #0b5cad; text-decoration: none; font-weight: 600; }
                    a:hover { text-decoration: underline; }
                    .empty { padding: 16px; background: #ffffff; border: 1px solid #d9e2ec; }
                  </style>
                </head>
                <body>
                <main>
                  <h1>""").append(escapeHtml(pageTitle)).append("</h1>\n");

        if (documents.isEmpty()) {
            html.append("  <div class=\"empty\">No documentation is registered.</div>\n");
        } else {
            html.append("""
                      <table>
                        <thead>
                        <tr><th>Title</th><th>Category</th><th>Type</th><th>Description</th></tr>
                        </thead>
                        <tbody>
                    """);
            for (ScmDocDescriptor document : documents) {
                String href = StringUtils.hasText(document.href())
                        ? document.href()
                        : normalizedBasePath + "/api/" + urlEncode(document.id());
                html.append("    <tr><td><a href=\"")
                        .append(escapeHtml(href))
                        .append("\">")
                        .append(escapeHtml(document.titleFor(language)))
                        .append("</a></td><td>")
                        .append(escapeHtml(document.category().name()))
                        .append("</td><td>")
                        .append(escapeHtml(document.type().name()))
                        .append("</td><td>")
                        .append(escapeHtml(document.descriptionFor(language)))
                        .append("</td></tr>\n");
            }
            html.append("""
                        </tbody>
                      </table>
                    """);
        }

        html.append("""
                </main>
                </body>
                </html>
                """);
        return html.toString();
    }

    private String normalizeBasePath(String basePath) {
        if (!StringUtils.hasText(basePath)) {
            return "/docs";
        }
        String normalized = basePath.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
