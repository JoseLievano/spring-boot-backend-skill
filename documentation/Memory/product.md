# Product

## Purpose
This project exists to build a reusable `spring-boot-backend` agent skill for **architecting and guiding the
implementation of** Spring Boot backend APIs. Three existing Spring Boot projects serve as reference
material — by studying them, we extract the architectural patterns that work well and encode them into a
skill that any agent can invoke to design and build a new API with confidence.

The skill is a **project-agnostic convention**, not a scaffold: it ships **no code artifacts and no Spring
app**. It states rules, module **contracts** (interfaces, invariants, error modes) and pseudo-code only.
Project initialization is explicitly not its job, and it is not Claude-Code-specific.

## How it works
An agent invokes the `spring-boot-backend` skill, provides high-level intent (domain, entities, auth
requirements, etc.), and follows the convention to design and implement a well-structured Spring Boot
project or individual components (controllers, services, repositories, DTOs, security config, etc.). The
platform is described as contracts that the agent implements **fresh against current framework
documentation looked up at execution time** — so every project gets the same design without shipping or
copying version-pinned code, and the skill never teaches deprecated APIs.

## User experience goals
- Invoking the skill should feel like pairing with someone who has already built several Spring Boot
  APIs and knows exactly how to structure things
- Output should be production-ready, not just a tutorial skeleton
- The skill should explain architectural decisions, not just generate code blindly
- Developers should be able to trust the documented patterns as battle-tested
- Every rule the skill states should be traceable to a reference finding or an ADR

## Non-goals
- The skill will not generate frontend code, infrastructure config, or CI/CD pipelines
- The skill will not modify the three reference projects
- The skill is not a general-purpose Java code generator — it is Spring Boot REST API-specific
- The skill will not initialize a project or ship scaffolding code — it is a design convention
