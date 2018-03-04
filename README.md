# RxBackoff

This library is useful when you want to execute a retry operation using exponential backoff algorithm when a HTTP request fails.

For example...

```
--> GET
<-- 500

  ... wait 2000ms ...

--> GET (retry)
<-- 500

  ... wait 4000ms ...

--> GET (retry)
<-- 500

  ... wait 8000ms ...

--> GET (retry)
<-- 200
```

## Backoff time algorithm

The backoff time can be choose fixed intervals or according to the truncated exponential backoff algorithm.

### Fixed interval backoff

```java
// wait 2s, 2s, 2s...
RxBackoff(3 /* max retry times */, 2000L /* ms */)
```

### Exponential backoff

```java
RxBackoff(4, { retry ->
  // wait 1s, 2s, 4s, 5s (8s is truncated to 5s)
  2F.pow(retry - 1).toLong().times(1000L).coerceAtMost(5000L)
})
```

## Use RxBackoff

The RxBackoff class works as a function of RxJava2.
Simple code is follows.

```
retrofit.webapi()
  .retryWhen(RxBackoff(3, 2000L))
```

### Utility RxBackoff function

| Function | Description |
|----------|-------------|
| filter | Filters errors emitted by an ObservableSource |
| doOnRetry | Callback function called every time before retry processing |
| doOnAbort | Callback function called when giving up retry |


```java
retrofit.webapi()
  .retryWhen(
      RxBackoff(3, { retry ->
        2F.pow(retry - 1).toLong().times(1000L).coerceAtMost(5000L)
      })
          .filter { it is HttpException } // You can filtered 500 or 504 here
          .doOnRetry { e, cnt -> println("Retry $cnt times, error=$e") }
          .doOnAbort { e -> println("Abort, error=$e") })
```

## License

Copyright 2017 Matsumura Yuki. Licensed under the Apache License, Version 2.0;
