
# ParcelFlow CI Workflow

ParcelFlow uses GitHub Actions for continuous integration.

## Automated checks

Every push and pull request triggers these jobs:

- Build Spring Boot using Java 21 and Maven.
- Build React using Node.js 22 and npm.
- Build the backend and frontend Docker images.

## Development workflow

1. Create a feature branch.
2. Make and commit changes.
3. Push the branch to GitHub.
4. Open a pull request.
5. Check that the CI jobs succeed.
6. Merge the pull request into the main branch.

Automated tests will be expanded during the testing phase.
