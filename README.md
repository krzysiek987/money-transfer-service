# Transfer Service

## Preface


## About
Service exposing RESTful API for money transfer between accounts.

Requirements:
* Java 12
* Gradle

<aside class="warning">
Gradle wrapper is located in repository and it can be used instead of globally installed Gradle.
</aside>

## How to start
 
Application can be built with gradle
  
    gradle build
    
and run either with gradle
    
    gradle run
    
or after it was build you can run jar located in _build/lib_ directory like:

    java -jar transfer-service-1.0.0-SNAPSHOT.jar
    
By default server starts on port 80, but it can be changed with application.conf properties or environment variables. 
<aside class="warning">
Java 12 is required to run the Transaction Service
</aside>


## Available resources
### Transfer
Transfer resource is available under _/1.0/transfers_ path. Where 1.0 is API version.
#### Resource structure
    {
        "id": <UUID>,
        "status": <string - one of "SCHEDULED", "SUCCESS", "ERROR">,
        "sourceAccountId": <UUID>,
        "destinationAccountId": <UUID>,
        "value": <decimal>,
        "currency": <string - 3 digit currency code (any supported by java.util.Currency)>,
        "title": <string - optional>,
        "createdAt": <long - instant>,
        "scheduledAt": <string - timestamp in ISO_DATE_TIME format>
    }
#### Get Transfer list

Method returns all transfers matching filters. Currently only sourceAccountId is supported as a filter.  

    GET /1.0/transfers?sourceAccountId=d6896820-7f3e-11e9-b475-0800200c9a66

Example response:

    HTTP 200 OK
    [
        {
            "id": "3669cc1e-4aab-4a9a-91c5-458ecddedb37",
            "status": "SUCCESS",
            "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
            "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
            "value": 12.5,
            "currency": "PLN",
            "title": null,
            "createdAt": 1558875616.379254,
            "scheduledAt": null
        },
        {
            "id": "5f1535ca-7f06-4082-b3d7-8c9eb0c6e3f9",
            "status": "SCHEDULED",
            "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
            "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
            "value": 100,
            "currency": "PLN",
            "title": null,
            "createdAt": 1558875850.224001,
            "scheduledAt": 1575640272.123
        }
    ]
    
#### Get Transfer by id

Get Transfer resource by id 

    GET /1.0/transfers/3669cc1e-4aab-4a9a-91c5-458ecddedb37

Example success response:

    HTTP 200 OK
    {
        "id": "3669cc1e-4aab-4a9a-91c5-458ecddedb37",
        "status": "SUCCESS",
        "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
        "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
        "value": 12.5,
        "currency": "PLN",
        "title": null,
        "createdAt": 1558875616.379254,
        "scheduledAt": null
    }
    
Example error resource:

    HTTP 404 NOT_FOUND
    
#### Create Transfer

The request inserts a Transfer. If `scheduledAt` property is filled Transfer state will be set as `SCHEDULED` and accounts balances won't be changed until provided date. There is background task running that polls all scheduled Transfers and invokes them if neccessary.

    POST /1.0/transfers
    {
        "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
        "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
        "value": 12.5,
        "currency": "PLN"
    }

Example success response:

    HTTP 201 CREATED
    Location=http://localhost:80/1.0/transfers/d8a8be15-eaa2-4836-9b5e-6d6a3c55e8d2
    {
        "id": "d8a8be15-eaa2-4836-9b5e-6d6a3c55e8d2",
        "status": "SUCCESS",
        "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
        "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
        "value": 12.5,
        "currency": "PLN",
        "title": null,
        "createdAt": 1558875214.6056551,
        "scheduledAt": null
    }
    
Example error response:

    HTTP 400 BAD_REQUEST
    {
        "error": {
            "value": "EXPECTED_POSITIVE_VALUE",
            "sourceAccountId": "NOT_FOUND",
            "currency": "CURRENCY_DOES_NOT_MATCH_ACCOUNT"
        }
    }

#### Update Transfer

The request updates a Transfer. Only transfers that are in state _SCHEDULED_ can be updated because they were not yet invoked. Addtionally not all properties are allowed to be changed. In current API version you can modify properties:
* title
* value
* scheduleAt

Others even if will be provided will be just ignored.
 
    PUT /1.0/transfers/5f1535ca-7f06-4082-b3d7-8c9eb0c6e3f9
    { 
        "value": 100.5,
        "title": null,
        "scheduledAt": "2019-12-06T19:21:12.123+05:30[Asia/Calcutta]"
    }
    
Example success response:

    {
        "id": "5f1535ca-7f06-4082-b3d7-8c9eb0c6e3f9",
        "status": "SCHEDULED",
        "sourceAccountId": "d6896820-7f3e-11e9-b475-0800200c9a66",
        "destinationAccountId": "f18e5e50-7f3e-11e9-b475-0800200c9a66",
        "value": 100.5,
        "currency": "PLN",
        "title": null,
        "createdAt": 1558875850.224001,
        "scheduledAt": 1575640272.123
    }
    
Example error response:

    HTTP 400 BAD_REQUEST
    {
        "error": {
            "status": "NOT_SUPPORTED"
        }
    }
 
#### Delete Transfer

Request used to delete a Transfer. Only _SCHEDULED_ and _ERROR_ transfers can be deleted.
 
    DELETE /1.0/transfers/5f1535ca-7f06-4082-b3d7-8c9eb0c6e3f9
    
Example success response:

    HTTP 204 NO_CONTENT
    
Example error response:

    HTTP 403 FORBIDDEN

or

    HTTP 404 NOT_FOUND
    
    
## Technical details
### Configuration
All supported configuration options are in `application.conf` file available in resources.
    
    server {
      port = 80
      host = localhost
    }
    dataSource {
      jdbcUrl = "jdbc:h2:mem:testdb"
      username = "sa"
      password = ""
    }
    
For more details see `pl.dorzak.transferservice.config` package. 
### Database
Unless default configuration will be overriden service will create in memory H2 database.
Database is initialized with _transfer_, _account_  and _account_transaction_leg_ tables and following Accounts are created:
* PLN - d6896820-7f3e-11e9-b475-0800200c9a66
* PLN - f18e5e50-7f3e-11e9-b475-0800200c9a66
* GBP - faab9700-7f3e-11e9-b475-0800200c9a66