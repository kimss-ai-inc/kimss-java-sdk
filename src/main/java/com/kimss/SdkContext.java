package com.kimss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Build {@code X-Kimss-SDK-Context} for gateway clients (OpenAI/Anthropic-compatible).
 */
public final class SdkContext {
    private SdkContext() {}

    public static String hostEnvironment() {
        String site = trim(System.getenv("WEBSITE_SITE_NAME"));
        if (!site.isEmpty()) {
            return site;
        }
        String lambda = trim(System.getenv("AWS_LAMBDA_FUNCTION_NAME"));
        if (!lambda.isEmpty()) {
            String region = trim(System.getenv("AWS_REGION"));
            if (region.isEmpty()) {
                region = trim(System.getenv("AWS_DEFAULT_REGION"));
            }
            return ("AWS Lambda:" + lambda + (region.isEmpty() ? "" : " (" + region + ")")).substring(0, Math.min(512, 512));
        }
        if (!trim(System.getenv("KUBERNETES_SERVICE_HOST")).isEmpty()) {
            String pod = trim(System.getenv("HOSTNAME"));
            return pod.isEmpty() ? "Kubernetes" : ("Kubernetes:" + pod);
        }
        String gcp = trim(System.getenv("K_SERVICE"));
        if (!gcp.isEmpty()) {
            return "GCP Cloud Run:" + gcp;
        }
        String gh = trim(System.getenv("GITHUB_REPOSITORY"));
        if (!gh.isEmpty()) {
            return "GitHub:" + gh;
        }
        if (!trim(System.getenv("GITHUB_ACTION")).isEmpty() || isTruthy(System.getenv("CI"))) {
            return "GitHub-CI";
        }
        try {
            String host = java.net.InetAddress.getLocalHost().getHostName();
            if (host != null && !host.isBlank() && !"localhost".equalsIgnoreCase(host)) {
                return "Host:" + host;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "Local/Dev";
    }

    public static String encodeHeader(String resourceType, String resourceName) {
        return encodeHeader(resourceType, resourceName, null, null, null);
    }

    public static String encodeHeader(
            String resourceType,
            String resourceName,
            String env,
            String region,
            String hostname
    ) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("host_environment", hostEnvironment());
        payload.put("resource_type", normalizeType(resourceType));
        payload.put("resource_name", trim(resourceName));
        String hn = trim(hostname);
        if (hn.isEmpty()) {
            try {
                hn = trim(java.net.InetAddress.getLocalHost().getHostName());
            } catch (Exception ignored) {
                hn = "";
            }
        }
        if (!hn.isEmpty()) {
            payload.put("hostname", hn.substring(0, Math.min(256, hn.length())));
        }
        String customerEnv = trim(env);
        if (customerEnv.isEmpty()) {
            customerEnv = trim(System.getenv("KIMSS_CALLER_ENV"));
        }
        if (customerEnv.isEmpty()) {
            customerEnv = trim(System.getenv("ENV"));
        }
        if (!customerEnv.isEmpty()) {
            payload.put("env", customerEnv.substring(0, Math.min(64, customerEnv.length())));
        }
        String reg = trim(region);
        if (reg.isEmpty()) {
            reg = trim(System.getenv("KIMSS_CALLER_REGION"));
        }
        if (reg.isEmpty()) {
            reg = trim(System.getenv("AWS_REGION"));
        }
        if (!reg.isEmpty()) {
            payload.put("region", reg.substring(0, Math.min(64, reg.length())));
        }
        String json = toJson(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, String> gatewayHeaders(String agentId, String agentName) {
        Map<String, String> headers = new LinkedHashMap<>();
        String aid = trim(agentId);
        if (aid.isEmpty()) {
            throw new IllegalArgumentException("agentId is required");
        }
        headers.put("X-Kimss-Agent-Id", aid);
        String name = trim(agentName);
        if (!name.isEmpty()) {
            headers.put("X-Kimss-Agent-Name", name);
        }
        headers.put("X-Kimss-SDK-Context", encodeHeader("agent", aid));
        return headers;
    }

    private static String normalizeType(String resourceType) {
        String rt = trim(resourceType).toLowerCase();
        return "model".equals(rt) ? "model" : "agent";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isTruthy(String value) {
        String v = trim(value).toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v) || "on".equals(v);
    }

    private static String toJson(Map<String, String> payload) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : payload.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(escape(e.getKey())).append("\":\"").append(escape(e.getValue())).append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
