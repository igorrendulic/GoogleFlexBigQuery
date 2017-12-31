# Logging REST events in BigQuery

Corresponding article on how to create and visualize REST API call reports: [Corresponding article](https://medium.com/p/579856dea9a9)  


## Curl commands for interacting with REST API:
 
```
curl -H "Content-Type: application/json" -X POST -d '{"time":"2017-12-25T14:26:50.466"}' http://localhost:8080/api/ping
```

```
curl -H "Content-Type: application/json" http://localhost:8080/api/ping
```

```
curl -H "Content-Type: application/json" -X POST -d '{"userName":"Igor"}' http://localhost:8080/api/greeting
```

```
curl -H "Content-Type: application/json" http://localhost:8080/api/greeting
``` 

## Deploy to Flex Engine

```
mvn appengine:deploy
```

or add to specific project if you have multiple google cloud accounts:

```
mvn appengine:deploy -Dapp.deploy.project=yourprojectid
```