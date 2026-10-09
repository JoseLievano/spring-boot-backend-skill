# Guide — Index

#doc #guide #index

## About the Guide

The Guide is the architecture and design convention for future Spring Boot REST APIs. It is the only
place where the rules of the convention are stated
([[ADRs/ADR-001-guide-is-the-source-of-truth|ADR-001]]): the Base Project and the Skill cite its Rule IDs
and never restate a rule. The three reference projects are its evidence, not its authority.

Rules are version-neutral. The support floor is the Spring Boot generation that is under open-source
support, with Java 21 or newer ([[ADRs/ADR-003-version-baseline-as-a-support-policy|ADR-003]]).
Version-specific detail lives in each document's Version Notes.

The format of every document is defined in [[Docs/Guide/Guide-Conventions]] and checked by
`python3 scripts/validate-analysis-docs.py guide`.

## Documents

A planned document is named in inline code. It becomes a link when its file is written.

| # | Document | Content | State |
|---|---|---|---|
| 01 | [[Docs/Guide/01-Principles-and-Baseline\|01-Principles-and-Baseline]] | Design principles, the version support policy, how rules are cited | draft |
| 02 | [[Docs/Guide/02-Project-Layout-and-Module-Boundaries\|02-Project-Layout-and-Module-Boundaries]] | `platform` and `features`, allowed dependencies, architecture tests | draft |
| 03 | [[Docs/Guide/03-Feature-Module-Anatomy\|03-Feature-Module-Anatomy]] | The fixed file set of a Feature Module; when to opt out of the CRUD base | draft |
| 04 | [[Docs/Guide/04-CRUD-Base-and-Service-Hooks\|04-CRUD-Base-and-Service-Hooks]] | Base controller and service contract, hooks, Access Policy, Row Scope, conditional writes | draft |
| 05 | [[Docs/Guide/05-API-Contract\|05-API-Contract]] | Resource naming, status codes, page response, conditional requests, download tickets | draft |
| 06 | [[Docs/Guide/06-Query-Engine\|06-Query-Engine]] | Query Profile, Row Scope, request forms, operators, bounds | draft |
| 07 | [[Docs/Guide/07-Domain-Model-and-Persistence\|07-Domain-Model-and-Persistence]] | Entities, concurrency token, auditing, constraints, migrations | draft |
| 08 | [[Docs/Guide/08-Identity-Authentication-and-Authorization\|08-Identity-Authentication-and-Authorization]] | Current User, token verification, local Token Issuer, User Directory, authorization ownership | draft |
| 09 | [[Docs/Guide/09-Errors-and-Validation\|09-Errors-and-Validation]] | Exception hierarchy, problem details, validation | draft |
| 10 | [[Docs/Guide/10-Object-Storage-and-Uploads\|10-Object-Storage-and-Uploads]] | Storage port and adapters, Upload Coordinator, download tickets | draft |
| 11 | [[Docs/Guide/11-Idempotency\|11-Idempotency]] | Keys, fingerprints, states, leases, replay | draft |
| 12 | [[Docs/Guide/12-Configuration-and-Secrets\|12-Configuration-and-Secrets]] | Typed properties, fail-fast validation, profiles, no literal secrets | draft |
| 13 | [[Docs/Guide/13-Testing-Strategy\|13-Testing-Strategy]] | Test layers, contract tests, authorization matrix, architecture tests | draft |
| 14 | [[Docs/Guide/14-Observability-and-Operations\|14-Observability-and-Operations]] | Health indicators, logging rules | draft |
| 15 | [[Docs/Guide/15-Recipe-Add-a-Feature\|15-Recipe-Add-a-Feature]] | End-to-end walkthrough of adding one Feature Module | draft |
| 16 | [[Docs/Guide/16-Traceability-Matrix\|16-Traceability-Matrix]] | Every in-scope reference finding mapped to the rules that prevent it | draft |

## Status

- **Draft documents:** 16
- **Version Notes not verified:** 63

## Reviews

None yet. The contract review of the Guide is written after document 16.

## Changelog

- **2026-10-05** — Documents 10–16 written as drafts: 88 rules (G10-01 to G10-26, G11-01 to G11-15,
  G12-01 to G12-14, G13-01 to G13-15, G14-01 to G14-13, G15-01 to G15-05) and the traceability matrix.
  Their names in documents 02–09 became links; the problem type `content-type-mismatch` was added to the
  registry of document 09.
- **2026-10-05** — Documents 06–09 written as drafts: 78 rules (G06-01 to G06-16, G07-01 to G07-19,
  G08-01 to G08-29, G09-01 to G09-14). Their names in documents 02–05 became links.
- **2026-10-04** — Documents 01–05 written as drafts: 63 rules (G01-01 to G01-09, G02-01 to G02-11,
  G03-01 to G03-10, G04-01 to G04-20, G05-01 to G05-13).
- **2026-10-04** — Index created with the sixteen planned documents. No Guide document is written yet.
