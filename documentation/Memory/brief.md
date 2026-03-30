# Project Brief

## What is this project?
This workspace contains three existing Spring Boot backend projects (auth-server, BugTracker, wpmanager)
used as reference material for building a reusable Claude Code skill. The skill will allow developers
to generate well-structured Spring Boot backend APIs following the best patterns extracted from these projects.

## Core goals
- Analyze the three Spring Boot projects (auth-server, BugTracker, wpmanager) in depth
- Extract common architectural patterns, conventions, and best practices shared across all three
- Identify differences and decide which approach is superior in each case
- Produce a comprehensive Claude Code skill (`spring-boot-backend`) that scaffolds new Spring Boot APIs
  following the best patterns discovered

## Scope boundaries
- The skill is for Spring Boot backend REST APIs only — no frontend, no mobile, no CLI tools
- The three reference projects are inputs, not outputs — they will NOT be modified
- The skill should generate production-quality boilerplate, not toy demos
- Out of scope: deployment/infrastructure (Docker, K8s), CI/CD pipeline generation

## Key constraints
<!-- Technical, timeline, team, or other constraints that shape decisions. -->
- Skill must work as a standalone Claude Code skill (markdown prompt file)
- Patterns must be grounded in the actual code found in the three projects, not invented

## Success criteria
- A skill file that, when invoked, reliably produces a well-structured Spring Boot project
  matching the best conventions extracted from the reference projects
- Documentation clearly explains which patterns were chosen and why
