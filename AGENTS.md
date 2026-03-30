# Project Overview

This repository is a skill-authoring workspace for a reusable `spring-boot-backend` agent skill.

It contains three Spring Boot backend projects used as reference material:
- `auth-server/`
- `BugTracker/`
- `wpmanager/`

The goal of this workspace is to analyze those reference projects, extract their best backend patterns, and use those findings to build a reusable skill in `spring-boot-skill/`.

# Agent Rules

- Always use the `memory-bank` skill.
- Always use the `documentation-management` skill when reading, creating, updating, or deleting markdown files, or when working with documentation files.
- Use `tree` when you need to understand directory structure.
- Use `rg --files` when you need a fast full file listing or need to locate files by path.
- Use `ls` for quick inspection of a specific directory.
