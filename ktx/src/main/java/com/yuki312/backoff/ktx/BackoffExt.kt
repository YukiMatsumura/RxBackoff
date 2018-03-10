package com.yuki312.backoff.ktx

import com.yuki312.backoff.core.BinaryExponentialAlgorithm
import com.yuki312.backoff.core.ExponentialAlgorithm
import com.yuki312.backoff.core.RandomIntervalAlgorithm

fun exponentialBackoff(
        interval: Long = ExponentialAlgorithm.DEFAULT_INTERVAL,
        multiplier: Double = ExponentialAlgorithm.DEFAULT_MULTIPLIER,
        maxInterval: Long = ExponentialAlgorithm.DEFAULT_MAX_INTERVAL,
        range: Double = ExponentialAlgorithm.DEFAULT_RANGE
): ExponentialAlgorithm {
    return ExponentialAlgorithm(interval, multiplier, maxInterval, range)
}

fun binaryExponentialBackoff(
        interval: Long = ExponentialAlgorithm.DEFAULT_INTERVAL,
        maxInterval: Long = ExponentialAlgorithm.DEFAULT_MAX_INTERVAL,
        range: Double = ExponentialAlgorithm.DEFAULT_RANGE
): BinaryExponentialAlgorithm {
    return BinaryExponentialAlgorithm(interval, maxInterval, range)
}

fun randomInterval(
        lowInterval: Long = RandomIntervalAlgorithm.DEFAULT_LOW_INTERVAL,
        highInterval: Long = RandomIntervalAlgorithm.DEFAULT_HIGH_INTERVAL,
        lowMultiplier: Double = RandomIntervalAlgorithm.DEFAULT_LOW_MULTIPLIER,
        highMultiplier: Double = RandomIntervalAlgorithm.DEFAULT_HIGH_MULTIPLIER,
        maxInterval: Long = RandomIntervalAlgorithm.DEFAULT_MAX_INTERVAL
): RandomIntervalAlgorithm {
    return RandomIntervalAlgorithm(
            lowInterval, highInterval, lowMultiplier, highMultiplier, maxInterval)
}
