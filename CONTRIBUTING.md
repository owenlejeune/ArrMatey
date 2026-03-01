# Contributing to ArrMatey

Thank you for your interest in contributing!

## Code of Conduct

Be respectful and constructive in all interactions.

## Issue & Workflow Guidelines

- Do not start any major feature development work unless there is an approved issue associated with it. Bug fixes or small features should still have issues opened but don't need approval first.
- If an issue already has an assignee, check in with that assignee before beginning any work.
- If an issue is open and unassigned:
    - Add a comment indicating that you’d like to work on it.
    - Include a brief description of your proposed implementation if applicable.
- If there is no open issue:
    - Create a new issue first, including all required details and a proposal for the implementation.
    - For large featurea, wait for the issue to be approved before starting development. For small features or buts feel free to start working right away!


## Development Setup

See the main README for setup instructions.

## Pull Request Process

1. Ensure there is an approved issue associated with the work (except for trivial docs-only changes).
2. Update documentation for any user-facing changes.
3. Follow Kotlin/Swift coding conventions.
4. Update the README if needed.
5. Request review from maintainers.

### Branching & Target Rules

- **Bug fixes and small changes**
    - Open pull requests directly against `main`.

- **Larger features** (e.g., new integrations or substantial refactors)
    - Create and target a dedicated feature branch for all related PRs.
    - Once all work for the feature is complete, open a PR to merge the feature branch into `main`.

### UI Work Across Platforms

- For any UI-related tasks, implement the changes on both iOS and Android when applicable.
- If you can only work on one platform:
    - Target a feature branch with your PR.
    - That feature branch should only be merged into `main` once both platforms are complete.

## Commit Messages

Use conventional commits:

- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation only
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Maintenance tasks
