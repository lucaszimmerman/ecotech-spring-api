package com.ecotech.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Endereco publico do frontend. O login com Google redireciona o navegador
 * para {@code <url>/auth/google} ao terminar.
 */
@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(
        String url
) {

    public String callbackUrl() {
        String base = url == null ? "" : url.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/auth/google";
    }
}
