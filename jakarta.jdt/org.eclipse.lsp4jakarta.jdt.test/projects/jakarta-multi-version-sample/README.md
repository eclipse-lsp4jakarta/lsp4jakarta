# Jakarta Multi-Version Sample

A single-module Maven project that mixes Jakarta EE **9**, **10**, and **11** API dependencies on the same classpath. Created to test [LSP4Jakarta](https://github.com/eclipse/lsp4jakarta) support for multiple Jakarta versions coexisting in a single project.

---

## Project Structure

```
jakarta-multi-version-sample/
├── pom.xml
└── src/main/java/org/example/
    ├── servlet/
    │   ├── SampleServlet.java        ← Jakarta EE 9 (Servlet 5.0 + Inject 2.0)
    │   └── GreetingService.java
    ├── persistence/
    │   ├── Product.java              ← Jakarta EE 10 (Persistence 3.1) + EE 11 (Validation 3.1)
    │   └── ProductRepository.java    ← Jakarta EE 10 (Persistence 3.1 + Annotation 2.1 + EJB 4.0)
    └── rest/
        ├── RestApplication.java      ← Jakarta EE 11 (REST/JAX-RS 4.0)
        └── ProductResource.java      ← Jakarta EE 11 (REST 4.0 + CDI 4.1 + Validation 3.1) + EE 9 (Inject 2.0)
```

---

## Jakarta EE Version Details

### Jakarta EE 9

> **Key change:** Namespace migration from `javax.*` → `jakarta.*` across all specs.

| Artifact | Version | Maven Coordinates |
|---|---|---|
| Jakarta Servlet API | `5.0.0` | `jakarta.servlet:jakarta.servlet-api:5.0.0` |
| Jakarta Inject API | `2.0.1` | `jakarta.inject:jakarta.inject-api:2.0.1` |

- **Servlet 5.0** — First release under the `jakarta.*` namespace. API is functionally equivalent to Servlet 4.0 (`javax.servlet`) but with the new namespace.
- **Inject 2.0** — Dependency injection annotations (`@Inject`, `@Named`, `@Qualifier`, etc.) migrated to `jakarta.inject`.

**Used in:** [`SampleServlet.java`](src/main/java/org/example/servlet/SampleServlet.java)

---

### Jakarta EE 10

> **Key change:** Requires Java SE 11 minimum. CDI made a core component. Removed deprecated APIs.

| Artifact | Version | Maven Coordinates |
|---|---|---|
| Jakarta Persistence API | `3.1.0` | `jakarta.persistence:jakarta.persistence-api:3.1.0` |
| Jakarta Annotation API | `2.1.1` | `jakarta.annotation:jakarta.annotation-api:2.1.1` |
| Jakarta EJB API | `4.0.1` | `jakarta.ejb:jakarta.ejb-api:4.0.1` |

- **Persistence 3.1** — Added `UUID` as a basic type, new `@IdClass` improvements, new JPQL functions (`CEILING`, `FLOOR`, `ROUND`, `EXP`, `LN`, `POWER`, `SIGN`).
- **Annotation 2.1** — Common annotations (`@PostConstruct`, `@PreDestroy`, `@Resource`, etc.) updated for EE 10 compatibility.
- **EJB 4.0** — Removed deprecated EJB 2.x entity beans and associated APIs. Retained stateless, stateful, and singleton session beans.

**Used in:** [`Product.java`](src/main/java/org/example/persistence/Product.java), [`ProductRepository.java`](src/main/java/org/example/persistence/ProductRepository.java)

---

### Jakarta EE 11

> **Key change:** Requires Java SE 21 minimum. Embraces virtual threads (Project Loom). Adds CDI-based integrations.

| Artifact | Version | Maven Coordinates |
|---|---|---|
| Jakarta Bean Validation API | `3.1.0` | `jakarta.validation:jakarta.validation-api:3.1.0` |
| Jakarta REST (JAX-RS) API | `4.0.0` | `jakarta.ws.rs:jakarta.ws.rs-api:4.0.0` |
| Jakarta CDI API | `4.1.0` | `jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0` |

- **Bean Validation 3.1** — New built-in constraints, improved integration with CDI and Jakarta Persistence. Added `@NotEmpty` and `@Email` enhancements.
- **REST (JAX-RS) 4.0** — Better alignment with CDI. Improved `@Context` injection alternatives using CDI. Enhanced `SeBootstrap` API for embedded deployments.
- **CDI 4.1** — Refined CDI Lite for build-time compatible extensions. Improved interoperability with virtual threads and improved `Event` API.

**Used in:** [`Product.java`](src/main/java/org/example/persistence/Product.java), [`ProductResource.java`](src/main/java/org/example/rest/ProductResource.java), [`RestApplication.java`](src/main/java/org/example/rest/RestApplication.java)

---

## Dependency Overview

All dependencies are declared with `scope: provided` — they are expected to be supplied by the application server at runtime and are available on the compile classpath for LSP4Jakarta to analyze.

```
EE 9   ──┬── jakarta.servlet-api       5.0.0
          └── jakarta.inject-api        2.0.1

EE 10  ──┬── jakarta.persistence-api   3.1.0
          ├── jakarta.annotation-api   2.1.1
          └── jakarta.ejb-api          4.0.1

EE 11  ──┬── jakarta.validation-api    3.1.0
          ├── jakarta.ws.rs-api        4.0.0
          └── jakarta.enterprise.cdi-api 4.1.0
```

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| Java | 17+ |
| Maven | 3.8+ |

> **Note:** Jakarta EE 11 specifications require Java 21 at runtime, but this project compiles with Java 17 since only the API jars (no runtime) are on the classpath.

---

## Build

```bash
cd jakarta-multi-version-sample
mvn compile
```

```bash
# Resolve and list all dependencies
mvn dependency:tree
```

---

## Purpose

This project is used to verify that **LSP4Jakarta** correctly resolves and provides diagnostics, code completions, and hover information for Jakarta APIs from different specification versions when they coexist in a single project's classpath.
