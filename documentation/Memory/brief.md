# Project Brief

## What is this project?
This workspace contains three existing Spring Boot backend projects (backend, BugTracker, wpmanager)
used as reference material for building a reusable `spring-boot-backend` agent skill. The skill is a
project-agnostic **convention** for Spring Boot backend REST APIs — rules, module contracts (interfaces,
invariants, error modes) and pseudo-code — extracted from the best patterns in these projects.

## Core goals
- Analyze the three Spring Boot projects (backend, BugTracker, wpmanager) in depth
- Extract common architectural patterns, conventions, and best practices shared across all three
- Identify differences and decide which approach is superior in each case
- Produce a comprehensive agent skill (`spring-boot-backend`) that states the convention — module
  contracts, rules and pseudo-code — so that new projects implement the contracts fresh against current
  framework documentation, following the best patterns discovered

## Scope boundaries
- The skill is for Spring Boot backend REST APIs only — no frontend, no mobile, no CLI tools
- The three reference projects are inputs, not outputs — they will NOT be modified
- The skill states production-quality design contracts, not toy demos
- The skill ships **no code artifacts and no Spring app**, and project initialization is not its job
- Out of scope: deployment/infrastructure (Docker, K8s), CI/CD pipeline generation

## Key constraints
<!-- Technical, timeline, team, or other constraints that shape decisions. -->
- Skill must work as a standalone skill directory (pure markdown — no code artifacts, no external tools)
- Patterns are grounded in the three reference projects and **corrected where the reviews found them
  wrong; every rule cites a finding ID or an ADR**
- The skill is not Claude-Code-specific; it is an agent skill expressed as markdown
- Spring Boot versioning is governed by a **support policy**: rules stay version-neutral, the floor is the
  currently OSS-supported Spring Boot generation, and version-specific details live in evidence-scoped
  version notes

## Success criteria
- A skill that, when invoked, reliably guides an agent to implement a well-structured Spring Boot project
  matching the best conventions extracted from the reference projects
- Documentation clearly explains which patterns were chosen and why

