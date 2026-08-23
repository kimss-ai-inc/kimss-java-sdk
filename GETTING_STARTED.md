# Getting started — Kimss Java Gateway

**Track, govern, and secure autonomous agents with exactly 1 line of code.**

## Step 1 — Sign In & Vault

Vault your provider under **Governance → Provider Vault** (Connected Infrastructure). Production requires `https://` URLs. Optional per-endpoint monthly token caps: [docs](https://kimss.ai/docs/custom_model_endpoints).

## Step 2 — Mint Key

**Gateway → Generate Key** → copy `kimss_...`.

## Step 3 — Route traffic

**OpenAI**

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl("https://api.kimss.ai/v1")
    .apiKey(System.getenv("KIMSS_WORKSPACE_KEY"))
    .putHeader("X-Kimss-Agent-Id", System.getenv("KIMSS_AGENT_ID"))
    .putHeader("X-Kimss-Agent-Name", "My Agent")
    .build();
```

**Anthropic** — `baseUrl("https://api.kimss.ai")` (the official client appends `/v1/messages`):

```java
AnthropicClient client = AnthropicOkHttpClient.builder()
    .baseUrl("https://api.kimss.ai")
    .apiKey(System.getenv("KIMSS_WORKSPACE_KEY"))
    .putHeader("X-Kimss-Agent-Id", System.getenv("KIMSS_AGENT_ID"))
    .putHeader("X-Kimss-Agent-Name", "My Agent")
    .build();
```

Do not use `KimssClient` for chat/completions/messages. Azure official clients are not inbound. Kill switch: HTTP 403 `agent_disabled`.

See [AI_INTEGRATION.md](AI_INTEGRATION.md).
