package com.example.crm.userauth.config;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerUiController {

    private static final String SWAGGER_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>CRM API - Swagger UI</title>
                <link rel="stylesheet" type="text/css" href="/webjars/swagger-ui/5.13.0/swagger-ui.css" />
                <link rel="icon" type="image/png" href="/webjars/swagger-ui/5.13.0/favicon-32x32.png" sizes="32x32" />
                <link rel="icon" type="image/png" href="/webjars/swagger-ui/5.13.0/favicon-16x16.png" sizes="16x16" />
                <style>
                    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }
                    *, *:before, *:after { box-sizing: inherit; }
                    body { margin: 0; background: #fafafa; }
                </style>
            </head>
            <body>
                <div id="swagger-ui"></div>
                <script src="/webjars/swagger-ui/5.13.0/swagger-ui-bundle.js" charset="UTF-8"></script>
                <script src="/webjars/swagger-ui/5.13.0/swagger-ui-standalone-preset.js" charset="UTF-8"></script>
                <script>
                    window.onload = function() {
                        window.ui = SwaggerUIBundle({
                            url: "/v3/api-docs",
                            dom_id: '#swagger-ui',
                            deepLinking: true,
                            presets: [
                                SwaggerUIBundle.presets.apis,
                                SwaggerUIStandalonePreset
                            ],
                            plugins: [
                                SwaggerUIBundle.plugins.DownloadUrl
                            ],
                            layout: "StandaloneLayout",
                            docExpansion: "list",
                            defaultModelsExpandDepth: 2,
                            defaultModelExpandDepth: 2,
                            tagsSorter: "alpha",
                            operationsSorter: "method"
                        });
                    };
                </script>
            </body>
            </html>
            """;

    @GetMapping(value = "/swagger-ui", produces = MediaType.TEXT_HTML_VALUE)
    public String swaggerUi() {
        return SWAGGER_HTML;
    }

    @GetMapping(value = "/swagger-ui.html", produces = MediaType.TEXT_HTML_VALUE)
    public String swaggerUiHtml() {
        return SWAGGER_HTML;
    }
}
