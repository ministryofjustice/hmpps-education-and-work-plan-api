# hmpps-education-and-work-plan-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-education-and-work-plan-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-education-and-work-plan-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-education-and-work-plan-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-education-and-work-plan-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-education-and-work-plan-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-education-and-work-plan-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-education-and-work-plan-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

## About
A Kotlin application providing APIs to support managing Work Plans for prisoners.

### Team
This application is in development by the Farsight Consulting team `Personal Learning Plans (PLP)`. They can be contacted on MOJ Slack channel `#plp-dev`.

### Health
The application has a health endpoint found at `/health` which indicates if the app is running and is healthy.

### Ping
The application has a ping endpoint found at `/ping` which indicates that the app is responding to requests.

## Configuring the project

### Ktlint formatting
Ktlint is used to format the source code and a task runs in the Circle build to check the formatting.

You should run the following commands to make sure that the source code is formatted locally before it breaks the Circle build.

#### Apply ktlint formatting rules to Intellij
`./gradlew ktlintApplyToIdea`

Or to apply to all Intellij projects:

`./gradlew ktlintApplyToIdeaGlobally`

#### Run ktlint formatter on git commit
`./gradlew addKtlintFormatGitPreCommitHook`

## Running the app
The easiest (and slowest) way to run the app is to use docker compose to create the service and all dependencies.

`docker-compose pull`

`docker-compose up`

See `http://localhost:8080/health` to check the app is running.
