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
    spec: {},
    docExpansion: "list",
    defaultModelsExpandDepth: 2,
    defaultModelExpandDepth: 2,
    tagsSorter: "alpha",
    operationsSorter: "method"
  });
};
