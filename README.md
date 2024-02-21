# hmpps-education-and-work-plan-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-education-and-work-plan-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-education-and-work-plan-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-education-and-work-plan-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-education-and-work-plan-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-education-and-work-plan-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-education-and-work-plan-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-education-and-work-plan-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html?configUrl=/v3/api-docs)

## About
A Kotlin application providing APIs to support managing Education And Work Plans for prisoners.

### Team
This application is in development by the Farsight Consulting team `Personal Learning Plans (PLP)`. They can be contacted on MOJ Slack channel `#plp-dev`.

### Health
The application has a health endpoint found at `/health` which indicates if the app is running and is healthy.

### Ping
The application has a ping endpoint found at `/ping` which indicates that the app is responding to requests.

## Configuring the project

### JDK
To develop and build the application locally you will need JDK 21 installed and configured.

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

* See `http://localhost:8081/health` to check the app is running.
* See `http://localhost:8081/swagger-ui/index.html?configUrl=/v3/api-docs` to explore the OpenAPI spec document.

## Environment variables
The following environment variables are required in order for the app to start:

### General

| Name           | Description                                |
|----------------|--------------------------------------------|
| SERVER_PORT    | The port that the application will run on  |
| HMPPS_AUTH_URL | The URL for OAuth 2.0 authorisation server |

### Database

| Name      | Description                       |
|-----------|-----------------------------------|
| DB_SERVER | The host of the DB server         |
| DB_NAME   | The name of the database instance |        
| DB_USER   | The application's DB username     |
| DB_PASS   | The DB user's password            |

### Application Insights

| Name                                   | Description                              |
|----------------------------------------|------------------------------------------|
| APPINSIGHTS_INSTRUMENTATIONKEY         | The instrumentation key for App Insights |
| APPLICATIONINSIGHTS_CONNECTION_STRING  | The connection string for App Insights   |
| APPLICATIONINSIGHTS_CONFIGURATION_FILE | A configuration file for App Insights    |

### APIs

| Name                   | Description                                                                                     |
|------------------------|-------------------------------------------------------------------------------------------------|
| CIAG_INDUCTION_API_URL | The URL of the CIAG Induction API, used as part of the ETL for the CIAG Induction API migration |
| CIAG_API_CLIENT_ID     | hmpps-auth oauth2 client-id for connecting to the CIAG Induction API                            |
| CIAG_API_CLIENT_SECRET | hmpps-auth oauth2 client-secret for connecting to the CIAG Induction API                        |

## Monitoring, tracing and event reporting
The API is instrumented with the opentelemetry and Application Insights java agent. Useful data can be found and reported
on via the Azure Application Insights console, all under the `cloud_RoleName` property of `hmpps-education-and-work-plan-api`.

The Application Insights console and the Kusto Query Language will be your friend here, but some example queries are
described below.

All example query links below are for `nomisapi-t3` (dev) - you will need to change the scope in order to target
preprod or prod.

### HTTP requests
Each HTTP request is logged in Application Insights. These can be [queried](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAAz3NMQrDMAyF4b2n0ObJ5AS5QoZeoKi2wCa2pUo2IdDDN87Q8cH385Q%252Bg6zb4wtHIiUIhUd8PbnQhpVgXcGlKmKe4gjYMzePLfqDdfdS8FqS3b9us0lo4BYMN57GJmCNpPA%252Boed6PWKVHzlVQVF8AAAA/timespan/P1D) as follows:

The `where name` clause is significant, otherwise the query will also return hits to the `/health` and `/info` endpoints.
```Kusto Query Language
requests
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| where name has '/action-plans'
| order by timestamp 
```

### Custom events
A number of custom events are triggered at various points in the API. The custom event names can be discovered with the
following [query](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAAw3LMQ6AIAwAwN1XdGPqExhdHfyAIdDERmgJFFl8vIw3XBzdtOwvifXtg3lTI4hZR7pOzXSEQuA9uLvU2pHSiMFYBYMknNoerDksVXZrJ%252B7GEg1ktR98jn%252FGWgAAAA%253D%253D/timespan/P1D)
```Kusto Query Language
customEvents
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| distinct name
```

### Projecting fields from custom dimensions
A query can include field projections, including projecting named fields from within the custom dimensions data.  
[For example](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAA22OOw7CMAyG957CW5eGG3QCVgYugEJiaCGJI8dphcThCS0tSz3Z%252Bvw%252FTE5C%252FjhgkFS9YeyQEYyjbC9ncnjSHqFtoe58jEmhzUZLT0HpYNVI%252FFTR6XLFvl7VYdHcSTtlGLXgl0amBxqBCspI7zGJ9rGZzrJKTtCCmfocCg2p5KTdTJpZxXgrCcHg1ucKF0uMe8pBtl1%252FsPQitshwff07fQB3NsotFQEAAA%253D%253D/timespan/P1D)
```Kusto Query Language
customEvents
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| where name == 'goal-create'
| project 
    timestamp,
    status = customDimensions.status, 
    reference = customDimensions.reference,
    stepCount = customDimensions.stepCount
| order by timestamp  
```

#### Charting data
Queries can contain aggregate functions, and the results rendered in charts. For example, [this query](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAA0WNMQ4CMQwEe17h7jiJIEF%252FNEBLwQeQL7G4iNiJEgcE4vEEJKD07sza1qKR91cSLbMn3CbKBDbE6k7HGOiATDAM0E2cUjHkqkX1UQyKM7eYLyYFbFfy3c%252BWr3OOGIzNhEr%252F1n4e7jyTlDZUlkUpbWMVhQ2sG1YqM2b%252FaOg7nfcw3mH0MtfmFEVOC1i5vpGZxFFuWKgsdsKsL5BxBSvOAAAA/timespan/P1D)
produces a column chart of the number of goals created per day that were created with more than 2 steps:
```Kusto Query Language
customEvents
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| where name == 'goal-create'
| where customDimensions.stepCount > 2
| summarize count() by bin(timestamp, 1d)
| render columnchart
```

### Joining data
Several objects within the Application Insights "tables" contain an `operation_Id` field which is effectively a correlation ID
between objects. For example, [this query](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAA6WPsU4DMQyG93sKbwdSw9D9WICBhYEXqEJi0bQXO9hOKyQenhDolYqRf4iS%252FP5%252B26GqcX44IJkOH3DcoiCEmWvcPPOMTz4jTBOM21yKOow1eEtMzlN0R5a9K7Nvr5LGhR6giU7gK%252FvZBUFvOHankRB61%252FuUkbSl6Y0aljuuZHAL65a040SwTxSnRIRSKb1V7PiVYLtqmxZ%252B9L%252BpzylFeIfBYPn6EheUzm4e4%252BrSqoryvebffU5eB677yXQRNiwNu2uNVfO5rIbf2a2KJaLAy%252Fu5BCJq%252BAQVaps8uQEAAA%253D%253D/timespan/P1D)
produces a report of usernames who have created goals with more than 2 steps. It does this by querying the `customEvents`
table for records whose name is `goal-create` and whose custom dimension field `stepCount` is greater than 2. It then joins
on the `requests` table on the `operation_Id` field, projecting out the custom dimension field `username` back out to the
outer query.
```Kusto Query Language
customEvents
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| where 
    name == 'goal-create'
    and customDimensions.stepCount > 2
| join kind=innerunique
    (requests
        | where cloud_RoleName == 'hmpps-education-and-work-plan-api'
        | project 
            operation_Id, 
            username = customDimensions.username
    )
    on operation_Id
| project
    timestamp,
    username
| order by timestamp desc 
```

#### JVM problems
There could be problems with the JVM used in the API, such as running out of threads or memory.

Various JVM related metrics are pushed to Application Insights by Spring/Micrometer. Run the following [query](https://portal.azure.com#@747381f4-e81f-4a43-bf68-ced6a1e14edf/blade/Microsoft_OperationsManagementSuite_Workspace/Logs.ReactView/resourceId/%2Fsubscriptions%2Fc27cfedb-f5e9-45e6-9642-0fad1a5c94e7%2FresourceGroups%2Fnomisapi-t3-rg%2Fproviders%2FMicrosoft.Insights%2Fcomponents%2Fnomisapi-t3/source/LogsBlade.AnalyticsShareLinkToQuery/q/H4sIAAAAAAAAAz3LsQ0CMQxA0Z4p3KXKCDcCFCyAQmIphjiObIcIieHvrqH8evp5mgtf0ZWyXX6wKipCbjLL4y4Nb4kRtg1C5TEsYpk5OUmPqZe4RN9xtHTUoPC%252F%252B%252FmYJ3Vb5BXC68OnihZUeH7BifFwHlDQ8g5UsB%252BLgwAAAA%253D%253D/timespan/P1D) to see what metrics are available:
```Kusto Query Language
customMetrics
| where cloud_RoleName == 'hmpps-education-and-work-plan-api'
| where name startswith 'jvm'
| order by timestamp desc 
```
For example, if the memory usage is consistently high you may need to increase the memory allocated in the `helm_dploy/hmpps-education-and-work-plan-api/values.yaml` file env var `JAVA_OPTS`.

## API external dependencies
This API consumes, and is therefore dependent on, data from the following APIs:

* `hmpps-auth` - Standard HMPPS Digital configuration; used for Spring Security.
* `application-insights` - Standard HMPPS Digital configuration; used for telemetry and event tracing.
* `prison-api` - The Prison API; used for looking up a Prisoner's prison history.

## API consumers
The following are the known consumers of this API. Any changes to this API - especially breaking or potentially breaking
changes should consider the use case of these consumers.

As is standard in HMPPS projects the term "service UI" specifically means the node/express service codebase; and not the
UI running in the browser. UI's running in the browser do not make xhr/ajax style requests directly to the APIs.

* `hmpps-eduction-and-work-plan-ui` - The Education and Work Plan service UI (aka PLP)  
The REST API endpoints exposed by this API are consumed by the service UI. Roles required are `ROLE_EDUCATION_WORK_PLAN_EDITOR` or
`ROLE_EDUCATION_WORK_PLAN_VIEWER` - see the individual controller methods and/or swagger spec for specific details.
* `hmpps-ciag-careers-induction-ui` - The CIAG Induction service UI  
The CIAG Induction service UI consumes the `GET` endpoint in order to retrieve action plan details for a list of prison
numbers (prisoners). The role required is `ROLE_EDUCATION_WORK_PLAN_VIEWER`

## Feature Toggles
Features can be toggled by setting the relevant environment variable.

| Name                                  | Default Value | Type    | Description                                                                                                          |
|---------------------------------------|---------------|---------|----------------------------------------------------------------------------------------------------------------------|
| SOME_TOGGLE_ENABLED                   | false         | Boolean | Example feature toggle, for demonstration purposes.                                                                  |
| CIAG_INDUCTION_DATA_MIGRATION_ENABLED | false         | Boolean | Set to true to enable the components that perform an ETL migration of CIAG Inductions from the CIAG API to this API. |
