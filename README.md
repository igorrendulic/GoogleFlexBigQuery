# Logging REST events in BigQuery



## Curl for sending an event to REST API:
 
```
curl -H "Content-Type: application/json" -X POST -d '{"id":"1","userId":"user2","time":"2017-12-25T14:26:50.466"}' http://localhost:8080/api/event
```

or without time (server modified LocalDateTime to current time): 

```
curl -H "Content-Type: application/json" -X POST -d '{"id":"2","userId":"user3"}' http://localhost:8080/api/event
``` 

## Deploy to Flex Engine

```
mvn appengine:deploy
```

or add to specific project if you have multiple google cloud accounts:

```
mvn appengine:deploy -Dapp.deploy.project=yourprojectid
```