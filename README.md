# liknot

Experimental-implementation K8s Ingress Controller with :

- Spring Boot with Kotlin
- gRPC with Armeria
- Envoy

## Memo

### How to try on your computer

```shell
skaffold dev
```

### Result

(You need to port-forward to the envoy in daemonset pod)

```shell
~ | on kind-liknot
 ❯❯ curl -v localhost:8080/bar -H 'Host: foo.bar.com'
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /bar HTTP/1.1
> Host: foo.bar.com
> User-Agent: curl/7.79.1
> Accept: */*
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 404 Not Found
< vary: Origin,Access-Control-Request-Method,Access-Control-Request-Headers
< content-type: application/json
< date: Wed, 14 Dec 2022 12:34:57 GMT
< x-envoy-upstream-service-time: 1402
< server: envoy
< transfer-encoding: chunked
<
* Connection #0 to host localhost left intact
{"timestamp":"2022-12-14T12:34:56.893+00:00","status":404,"error":"Not Found","path":"/bar"}%
```
