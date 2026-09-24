# Jakarta EE9 + EE10 Sample

A single-module Maven project that mixes Jakarta EE **9** and **10** API dependencies on the same classpath. Created to test [LSP4Jakarta](https://github.com/eclipse/lsp4jakarta) support for multiple Jakarta versions coexisting in a single project.

---

## Project Structure

```
jakarta-ee9-ee10-sample/
├── pom.xml
└── src/main/java/org/example/
    ├── servlet/
    │   ├── SampleServlet.java        ← Jakarta EE 9 (Servlet 5.0 + Inject 2.0)
    │   └── GreetingService.java
    ├── websocket/
    │   └── ChatEndpoint.java         ← Jakarta EE 9 (WebSocket 2.0)
    ├── persistence/
    │   ├── Product.java              ← Jakarta EE 10 (Persistence 3.1 + Validation 3.0)
    │   └── ProductRepository.java    ← Jakarta EE 10 (Persistence 3.1 + Annotation 2.1 + EJB 4.0)
    └── rest/
        ├── RestApplication.java      ← Jakarta EE 10 (REST/JAX-RS 3.1)
        └── ProductResource.java      ← Jakarta EE 10 (REST 3.1 + CDI 4.0 + Validation 3.0) + EE 9 (Inject 2.0)
```

---

## Jakarta EE Version Details

### Jakarta EE 9

> **Key change:** Namespace migration from `javax.*` → `jakarta.*` across all specs. API surface remains largely unchanged from Java EE 8.

| Artifact | Version | Maven Coordinates |
|---|---|---|
| Jakarta Servlet API | `5.0.0` | `jakarta.servlet:jakarta.servlet-api:5.0.0` |
| Jakarta Inject API | `2.0.1` | `jakarta.inject:jakarta.inject-api:2.0.1` |
| Jakarta WebSocket API | `2.0.0` | `jakarta.websocket:jakarta.websocket-api:2.0.0` |

- **Servlet 5.0** — First release under the `jakarta.*` namespace. Functionally equivalent to Servlet 4.0 (`javax.servlet`) with the new namespace applied.
- **Inject 2.0** — Dependency injection annotations (`@Inject`, `@Named`, `@Qualifier`, `@Singleton`) migrated to `jakarta.inject`.
- **WebSocket 2.0** — Server and client WebSocket API migrated to `jakarta.websocket`. Annotations: `@ServerEndpoint`, `@OnOpen`, `@OnMessage`, `@OnClose`, `@OnError`.

**Used in:** [`SampleServlet.java`](src/main/java/org/example/servlet/SampleServlet.java), [`ChatEndpoint.java`](src/main/java/org/example/websocket/ChatEndpoint.java)

---

### Jakarta EE 10

> **Key change:** Requires Java SE 11 minimum. CDI made a core component of the platform. Deprecated APIs from earlier specs removed.

| Artifact | Version | Maven Coordinates |
|---|---|---|
| Jakarta Persistence API | `3.1.0` | `jakarta.persistence:jakarta.persistence-api:3.1.0` |
| Jakarta Annotation API | `2.1.1` | `jakarta.annotation:jakarta.annotation-api:2.1.1` |
| Jakarta EJB API | `4.0.1` | `jakarta.ejb:jakarta.ejb-api:4.0.1` |
| Jakarta Bean Validation API | `3.0.2` | `jakarta.validation:jakarta.validation-api:3.0.2` |
| Jakarta REST (JAX-RS) API | `3.1.0` | `jakarta.ws.rs:jakarta.ws.rs-api:3.1.0` |
| Jakarta CDI API | `4.0.1` | `jakarta.enterprise:jakarta.enterprise.cdi-api:4.0.1` |

- **Persistence 3.1** — Added `UUID` as a basic type, new `@IdClass` improvements, new JPQL functions (`CEILING`, `FLOOR`, `ROUND`, `EXP`, `LN`, `POWER`, `SIGN`).
- **Annotation 2.1** — Common lifecycle annotations (`@PostConstruct`, `@PreDestroy`, `@Resource`) updated for EE 10 compatibility.
- **EJB 4.0** — Removed deprecated EJB 2.x entity beans. Retained stateless, stateful, and singleton session beans.
- **Bean Validation 3.0** — Aligned with EE 10; improved integration with CDI and Jakarta Persistence.
- **REST (JAX-RS) 3.1** — Better CDI alignment, `SeBootstrap` API for embedded use, improved multipart support.
- **CDI 4.0** — Introduced CDI Lite for build-time compatible extensions. Refined bean discovery and scope handling.

**Used in:** [`Product.java`](src/main/java/org/example/persistence/Product.java), [`ProductRepository.java`](src/main/java/org/example/persistence/ProductRepository.java), [`ProductResource.java`](src/main/java/org/example/rest/ProductResource.java), [`RestApplication.java`](src/main/java/org/example/rest/RestApplication.java)

---

## Dependency Overview

All dependencies are declared with `scope: provided` — they are expected to be supplied by the application server at runtime and are available on the compile classpath for LSP4Jakarta to analyze.

```
EE 9   ──┬── jakarta.servlet-api        5.0.0
          ├── jakarta.inject-api         2.0.1
          └── jakarta.websocket-api      2.0.0

EE 10  ──┬── jakarta.persistence-api    3.1.0
          ├── jakarta.annotation-api     2.1.1
          ├── jakarta.ejb-api            4.0.1
          ├── jakarta.validation-api     3.0.2
          ├── jakarta.ws.rs-api          3.1.0
          └── jakarta.enterprise.cdi-api 4.0.1
```

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| Java | 11+ |
| Maven | 3.8+ |

---

## Build

```bash
cd jakarta-ee9-ee10-sample
mvn compile
```

```bash
# Resolve and list all dependencies
mvn dependency:tree
```

---

## Purpose

This project is used to verify that **LSP4Jakarta** correctly resolves and provides diagnostics, code completions, and hover information for Jakarta APIs from EE 9 and EE 10 specification versions when they coexist in a single project's classpath.
