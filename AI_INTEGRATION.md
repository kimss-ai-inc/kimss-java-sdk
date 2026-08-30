# AI Integration — Kimss Gateway (Agent-to-Agent)

**Strict directive for coding assistants.**

> **Canonical public A2A contract:**  
> https://raw.githubusercontent.com/kimss-ai/kimss-control-plane/main/AI_INTEGRATION.md  
> ([kimss-ai/kimss-control-plane](https://github.com/kimss-ai/kimss-control-plane))

1. Never use `KimssClient` for chat/completions/messages.
2. Keep the developer's native OpenAI or Anthropic SDK.
3. OpenAI → `baseUrl("https://api.kimss.ai/v1")`.
4. Anthropic → `baseUrl("https://api.kimss.ai")` (SDK appends `/v1/messages`).
5. Always inject `X-Kimss-Agent-Id` / `X-Kimss-Agent-Name`.

Kill switch: `agent_disabled`.
