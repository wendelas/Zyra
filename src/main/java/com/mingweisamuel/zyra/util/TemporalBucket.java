package com.mingweisamuel.zyra.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * <p>This class represents a token bucket system. One instance represents one recurring bucket with a certain
 * limit of tokens per timespan.</p>
 */
public abstract class TemporalBucket {
    /**
     * Get the approximate delay til the next available token, or -1 if a token is available.
     * @return Approximate delay in milliseconds or -1.
     */
    public abstract long getDelay();

    /**
     * Gets a token, regardless of whether one is available.
     * @return True if the token was obtained without violating limits, false otherwise.
     */
    public boolean getToken() {
        return getTokens(1);
    }

    /**
     * Gets n tokens, regardless of whether they are available.
     * @return True if the tokens were obtained without violating limits, false otherwise.
     */
    public abstract boolean getTokens(int n);

    /**
     * Attempts to get a token from every bucket, or no tokens at all. Will synchronize on each instance
     * recursively.
     * @param buckets Buckets to get tokens from.
     * @return -1 if tokens were obtained, otherwise the approximate delay until tokens will be available.
     */
    public static long getAllTokensOrDelay(TemporalBucket... buckets) {
        // Always obtain locks in well-defined order to prevent deadlock. Sort by hash code.
        Arrays.sort(buckets, Comparator.comparingLong(TemporalBucket::hashCode));
        int i = getAllInternal(buckets, 0);
        if (i < 0) // Success
            return -1;
        // If there was delay, find the maximum or zero. This may be inaccurate due to buckets changing state
        // but that is inevitable unless we block the locks, but its better to let other threads through.
        long delay = 0;
        for (; i < buckets.length; i++) {
            long newDelay = buckets[i].getDelay();
            if (newDelay > delay)
                delay = newDelay;
        }
        return delay;
    }
    /**
     * @param buckets Buckets to check.
     * @param i Index of current bucket (for recursion).
     * @return -1 if all tokens were obtained or the index of the first limiting bucket.
     */
    private static int getAllInternal(TemporalBucket[] buckets, int i) {
        // Base case: No more buckets.
        if (i >= buckets.length)
            return -1;
        // Synchronize on the current bucket.
        synchronized (buckets[i]) {
            long delay = buckets[i].getDelay();
            if (delay >= 0) // This bucket has delay, exit synchronization immediately.
                return i;
            // A token is available for the current bucket.
            int index = getAllInternal(buckets, i + 1);
            if (index >= 0) // A later bucket has delay, exit synchronization immediately.
                return index;
            // Success.
            buckets[i].getToken();
            return -1;
        }
    }
}