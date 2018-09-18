# Small project for studing scala

## Server
Server connects to upstream by tcp and gets data from it. 
Then it aggregates data by minutes.

##Client
Client connects to server and first gets aggregated data for 10 (this parameter can be changed via `application.conf`) 
previous minutes. After that server pushes new data to client every minute.

## How to run
- run upstream.py `python upstream.py`
- run server `sbt "runMain server.ServerApp"`
- run clients `sbt "runMain client.ClientApp"`

## Run and stop with docker-compose
```
docker-compose up

docker-compose down -v
```