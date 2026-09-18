# Osebo Kotlin — Development Workflow

This document covers how the team works on the `dev` branch.

## Branching

- `dev` is the active development / staging branch — this is where UI and
  feature work lands before it's verified and promoted further.
- **Never commit directly to `dev`.** Branch off it for your work, then open
  a pull request back into it.
- Suggested branch naming: `feature/<short-description>` or
  `fix/<short-description>`.

## Pull requests

- Every PR into `dev` requires at least one review before merging.
- Keep PRs scoped to one feature or fix — easier to review, easier to
  revert if something's wrong.
- For any UI change, include a screenshot or short screen recording in the
  PR description.

## Before opening a PR

- Make sure the project builds clean locally and any existing tests pass.
- Resolve merge conflicts against the current `dev` before requesting review,
  not after.

## Commit messages

- Write clear, descriptive commit messages that explain *why* a change was
  made, not just what changed.
- Commits should be attributed to the person who actually wrote them —
  no AI/tool co-authorship or session trailers in commit history.

## Staging → Production

`dev` reflects what's being tested for the next release. Only promote to
the production branch once changes here have been verified.
