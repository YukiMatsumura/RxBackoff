
# RxBackoff  
  
This library is useful when you want a retry operation using **Exponential backoff algorithm**.  

> Exponential backoff is an algorithm that uses feedback to multiplicatively decrease the rate of some process, in order to gradually find an acceptable rate.
In a variety of computer networks, binary exponential backoff or truncated binary exponential backoff refers to an algorithm used to space out repeated retransmissions of the same block of data, often as part of network congestion avoidance.


### Usage

```gradle
implementation 'com.yuki312:RxBackoff:<latest version>'
```

[ ![Download](https://api.bintray.com/packages/yuki312/maven/RxBackoff/images/download.svg) ](https://bintray.com/yuki312/maven/RxBackoff/_latestVersion)

`RxBackoff` can be used for error handling using `retryWhen` function.  
The following code delays the retry process with the Binary Exponential Backoff algorithm.

```java
// retry -> (wait 0.5s) -> retry -> (wait 1s) -> retry -> (wait 2s) -> ...
retrofit.webapi()
    .retryWhen(RxBackoff.exponential(5 /* maxRetryCount */))  
    .subscribe(...)
```

The backoff algorithm allows you to choose the built-in one or choose your own one.

Fine options can also be specified:

```java
RxBackoff(Backoff.Builder()
    .setAlgorithm(ExponentialAlgorithm(
        5000 /* interval */,
        1.5 /* multiplier */,
        15_000L /* maxInterval */,
        1.2 /* range */))
    .setMaxElapsedTime(3, TimeUnit.MINUTES)
    .setMaxRetryCount(5)
    .build())
  .filter { it is HttpException }  // You can filtered 500 or 504 here 
  .doOnRetry { e, cnt ->  println("Retry $cnt times, error=$e")  }  
  .doOnAbort { e ->  println("Abort, error=$e")  })
```

## Backoff interval algorithm  

You can choose to set the Backoff interval to a specific value or a random value from a specific range. For HTTP request retry processing, a random interval is recommended to avoid congestion due to retries.  For local retries, random intervals may not be necessary.

### Exponential backoff  

![interval = interval * multiplier ^{RetryCount - 1}](https://latex.codecogs.com/svg.latex?\large&space;interval&space;=&space;interval&space;*&space;multiplier&space;^{RetryCount&space;-&space;1})

| *multiplier = 1.5* | 1st retry      | 2nd retry       | 3rd retry        |
| ------------------ | -------------- | --------------- | ---------------- |
| Interval           | 500 (400..600) | 1000 (600..900) | 1500 (900..1350) |

```java
com.yuki312.rxbackoff.ExponentialAlgorithm

// the default interval
public static final long DEFAULT_INTERVAL = 500L;

// the default multiplier (increases the interval by 50%)
public static final double DEFAULT_MULTIPLIER = 1.5;

// the default maximum interval. Truncate time that exceeds 15 seconds.
public static final long DEFAULT_MAX_INTERVAL = 15_000L;

// the default random range. choose randomly within the range of ± 20% of the interval value.
public static final double DEFAULT_RANGE = 0.2;

public ExponentialAlgorithm(long interval, double multiplier, long maxInterval, double range)
```

### Binary exponential backoff

![interval = interval * 2 ^{RetryCount - 1}](https://latex.codecogs.com/svg.latex?\large&space;interval&space;=&space;interval&space;*&space;2&space;^{RetryCount&space;-&space;1})

| *multiplier = 2.0* | 1st retry      | 2nd retry        | 3rd retry         |
| ------------------ | -------------- | ---------------- | ----------------- |
| Interval           | 500 (400..600) | 1000 (800..1200) | 2000 (1600..2400) |

```java
com.yuki312.rxbackoff.BinaryExponentialAlgorithm

// multiplier is fixed 2.0
public BinaryExponentialAlgorithm(long interval, long maxInterval, double range)
```

### Random interval backoff

![low = lowInterval * lowMultiplier ^{RetryCount - 1}](https://latex.codecogs.com/svg.latex?\large&space;low&space;=&space;lowInterval&space;*&space;lowMultiplier&space;^{RetryCount&space;-&space;1})  
![high = highInterval * highMultiplier ^{RetryCount - 1}](https://latex.codecogs.com/svg.latex?\large&space;high&space;=&space;highInterval&space;*&space;highMultiplier&space;^{RetryCount&space;-&space;1})  
![interval = Rand(low..high](https://latex.codecogs.com/svg.latex?\large&space;interval&space;=&space;Rand[low..high])

| *hMultiplier = 3.0* | 1st retry   | 2nd retry   | 3rd retry   |
| ------------------- | ----------- | ----------- | ----------- |
| Interval            | (500..1000) | (500..3000) | (500..9000) |

```java
com.yuki312.rxbackoff.RandomIntervalAlgorithm

// the default lower interval
public static final long DEFAULT_LOW_INTERVAL = 500L;

// the default high interval
public static final long DEFAULT_HIGH_INTERVAL = 1000L;

// the default multiplier (no increases the interval)
public static final double DEFAULT_LOW_MULTIPLIER = 1.0;

// the default multiplier (no increases the interval)
public static final double DEFAULT_HIGH_MULTIPLIER = 3.0;

// the default maximum interval. Truncate time that exceeds 15 seconds.
public static final long DEFAULT_MAX_INTERVAL = 15_000L;

public RandomIntervalAlgorithm(long lowInterval, long highInterval, 
                               double lowMultiplier, double highMultiplier, 
                               long maxInterval)
```

### Fixed interval backoff  

![interval = interval](https://latex.codecogs.com/svg.latex?\large&space;interval&space;=&space;interval)

| *multiplier = 0.0* | 1st retry      | 2nd retry      | 3rd retry      |
| ------------------ | -------------- | -------------- | -------------- |
| Interval           | 500            | 500            | 500            |

*This algorithm is not backed off.*

```java
com.yuki312.rxbackoff.FixedIntervalAlgorithm

// the default interval
public static final long DEFAULT_INTERVAL = 500L;

public FixedIntervalAlgorithm(long interval, TimeUnit unit)
```

### Custom interval backoff

You can also use your own backoff algorithm. If `Backoff.ABORT(0)` is returned, backoff processing will be aborted.  

```java
RxBackoff.of({ retry, elapsed ->  
  2F.pow(retry - 1).toLong().times(1000L).coerceAtMost(5000L)  
}, 5)
```

## Truncate interval times

The 'truncated' simply means that after a certain number of increases, the exponentiation stops; i.e. the retransmission timeout reaches a ceiling and thereafter does not increase any further.  
All built-in algorithms support Truncated.

```java
// the default maximum interval. Truncate time that exceeds 15 seconds.
public static final long DEFAULT_MAX_INTERVAL = 15_000L;

/**
 * @param maxInterval the maximum interval. Truncate time that exceeds
 **/
public BinaryExponentialAlgorithm(long interval, long maxInterval)
```

## Abort retry

You can abort retry processing when certain conditions are satisfied. RxBackoff measures the number of retry processes and elapsed time. When retry count or elapsed time exceeds the threshold value, the retry process is aborted.  

### Maximum retry counts & Maximum elapsed times

```java
com.yuki312.rxbackoff.Backoff.Builder

public Builder setMaxRetryCount(int count) {...}

public Builder setMaxElapsedTime(long elapsedTime, TimeUnit unit)
```

## Utility function  

### RxBackoff

| Function | Description |  
|----------|-------------|  
| exponential | Use Binary exponential backoff algorithm | 
| fixed | Use Fixed interval algorithm |
| of | Retry specified number of times with specified algorithm  |
| filter | Filters errors emitted by an ObservableSource |  
| doOnRetry | Callback function called every time before retry processing |  
| doOnAbort | Callback function called when giving up retry |  
  
  
```java  
retrofit.webapi()  
  .retryWhen(
      RxBackoff.exponential(maxRetryCount = 5))  
          .filter { it is HttpException } // You can filtered 500 or 504 here  
          .doOnRetry { e, cnt -> println("Retry $cnt times, error=$e") }  
          .doOnAbort { e -> println("Abort, error=$e") })  
```  



## License  
  
Copyright 2017 Matsumura Yuki. Licensed under the Apache License, Version 2.0;
