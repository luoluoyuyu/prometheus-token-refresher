# prometheus-token-refresher
Regularly update the tokenfile of Prometheus monitoring streamPipes, but Prometheus needs to enable - --web.enable-lifecycle.

## activate
Configuration arg is required. The specific args required are "SP_HOST" "SP_PORT" "SP_USERNAME" "SP_PASSWORD" "PROMETHEUS_HOST" "PROMETHEUS_PORT" "TOKEN_FILE". 
| Arg Name            | Description                           | Example                                      |
|---------------------|---------------------------------------|----------------------------------------------|
| SP_HOST             | Hostname for SP (Sample Parameter)    | "backed"                                     |
| SP_PORT             | Port for SP (Sample Parameter)        | 8080                                         |
| SP_USERNAME         | Username for SP (Sample Parameter)    | "admin@streampipes.apache.org"               |
| SP_PASSWORD         | Password for SP (Sample Parameter)    | "password"                                   |
| PROMETHEUS_HOST     | Prometheus hostname                   | "localhost"                                  |
| PROMETHEUS_PORT     | Prometheus port                       | 9090                                         |
| TOKEN_FILE          | Token file path                       | "/path/to/token"                             |


## docker
```
docker run -e SP_HOST="backed" \
           -e SP_PORT=8080 \
           -e SP_USERNAME="admin@streampipes.apache.org" \
           -e SP_PASSWORD="password" \
           -e PROMETHEUS_HOST="localhost" \
           -e PROMETHEUS_PORT=9090 \
           -e TOKEN_FILE="/path/to/token" \
            luoluoyuyu/prometheus-token-refresher:1.0.0
```
