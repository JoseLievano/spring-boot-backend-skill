# Product

## Purpose
This project exists to build a reusable Claude Code skill for generating Spring Boot backend APIs.
Three existing Spring Boot projects serve as reference material — by studying them, we extract the
architectural patterns that work well and encode them into a skill that any developer can invoke
to scaffold a new API with confidence.

## How it works
A developer invokes the `spring-boot-backend` skill, provides high-level intent (domain, entities,
auth requirements, etc.), and Claude uses the patterns documented here to generate a well-structured
Spring Boot project or individual components (controllers, services, repositories, DTOs, security
config, etc.) that follow the conventions extracted from real, proven codebases.

## User experience goals
- Invoking the skill should feel like pairing with someone who has already built several Spring Boot
  APIs and knows exactly how to structure things
- Output should be production-ready, not just a tutorial skeleton
- The skill should explain architectural decisions, not just generate code blindly
- Developers should be able to trust the generated patterns as battle-tested

## Non-goals
- The skill will not generate frontend code, infrastructure config, or CI/CD pipelines
- The skill will not modify the three reference projects
- The skill is not a general-purpose Java code generator — it is Spring Boot REST API-specific
