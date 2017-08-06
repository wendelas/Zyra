package com.mingweisamuel.zyra.util;

import com.mingweisamuel.zyra.ResponseListener;
import org.asynchttpclient.Response;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Represents rate limits for a particular region.
 */
public class RegionalRateLimiter {

    /** Represents the app rate limit. */
    private final TokenRateLimit applicationRateLimit;
    /** Represents method rate limits. */
    private final ConcurrentMap<String, TokenRateLimit> methodRateLimits = new ConcurrentHashMap<>();

    /** Max number of times to retry. */
    private final int maxRetries;
    /** Number of concurrent instances. */
    private final int concurrentInstances;
    /** Listens to HTTP responses. Can be null. */
    private final ResponseListener responseListener;

    public RegionalRateLimiter(int maxRetries, int concurrentInstances, ResponseListener responseListener,
            Map<Long, Integer> rateLimits) {

        int i = 0;
        TemporalBucket[] buckets = new TemporalBucket[rateLimits.size()];
        for (Map.Entry<Long, Integer> rateLimit : rateLimits.entrySet())
            buckets[i++] = new TokenTemporalBucket( // divide by concurrent instances.
                rateLimit.getKey(), rateLimit.getValue() / concurrentInstances, 20, 0.5f);

        this.maxRetries = maxRetries;
        this.concurrentInstances = concurrentInstances;
        this.responseListener = responseListener;
        this.applicationRateLimit = new TokenRateLimit(TokenRateLimit.RateLimitType.APPLICATION,
            concurrentInstances, buckets);
    }

    public RegionalRateLimiter(int maxRetries, int concurrentInstances, ResponseListener responseListener) {
        this.maxRetries = maxRetries;
        this.concurrentInstances = concurrentInstances;
        this.responseListener = responseListener;
        this.applicationRateLimit = new TokenRateLimit(TokenRateLimit.RateLimitType.APPLICATION,
            concurrentInstances);
    }

    public CompletableFuture<Response> getMethodRateLimited(final String methodId,
        final Supplier<CompletableFuture<Response>> supplier) {

        final TokenRateLimit methodRateLimit = getMethodRateLimit(methodId);

        RateLimitRetrierRunnable runnable = new RateLimitRetrierRunnable(supplier, methodRateLimit);
        runnable.run();
        return runnable.completion;
    }

    public CompletableFuture<Response> getRateLimited(final String methodId,
        final Supplier<CompletableFuture<Response>> supplier) {

        final TokenRateLimit methodRateLimit = getMethodRateLimit(methodId);

        RateLimitRetrierRunnable runnable = new RateLimitRetrierRunnable(supplier, applicationRateLimit, methodRateLimit);
        runnable.run();
        return runnable.completion;
    }

    private TokenRateLimit getMethodRateLimit(String methodId) {
        return methodRateLimits.computeIfAbsent(methodId, id -> new TokenRateLimit(
            TokenRateLimit.RateLimitType.METHOD, concurrentInstances));
    }

    /**
     * HTTP status codes that are considered a "success" in the sense that we did not violate limits
     * and the Riot API did its job without failing. The 20x will be deserialized, wile the 404 and 422
     * will return null.
     *
     * HTTP status codes that are retry-able: 400 (sometimes returned, ?), 429 (with valid retry headers), and
     * all 5xx responses.
     *
     * Other responses will throw a {@link RiotResponseException}.
     */
    private static final int[] RESPONSE_STATUS_SUCCESS = { 200, 204, 404, 422 };

    /**
     * A runnable to wait for rate limits before sending requests and getting responses.
     */
    private class RateLimitRetrierRunnable implements Runnable {

        /** Supplies response asynchronously. */
        private final Supplier<CompletableFuture<Response>> supplier;
        /** Rate limits to obey. */
        private final RateLimit[] rateLimits;

        /** The number of retries. (The first try is 0 retries). */
        private volatile int retries = 0;

        /** Future the response is passed to. */
        final CompletableFuture<Response> completion = new CompletableFuture<>();

        RateLimitRetrierRunnable(Supplier<CompletableFuture<Response>> supplier, RateLimit... rateLimits) {
            this.supplier = supplier;
            this.rateLimits = rateLimits;
        }

        @Override
        public void run() {
            long delay;
            if ((delay = RateLimit.getOrDelay(rateLimits)) < 0) { // Success.
                supplier.get()
                    .thenAccept(r -> {
                        int status = r.getStatusCode();
                        boolean success = 0 <= Arrays.binarySearch(RESPONSE_STATUS_SUCCESS, status);
                        if (responseListener != null)
                            responseListener.onResponse(success, r);

                        for (RateLimit rateLimit : rateLimits)
                            // If there is a 429 with invalid headers, the rate limiter will throw a RiotResponseException.
                            rateLimit.onResponse(r);

                        if (success) { // Success.
                            completion.complete(r);
                            return;
                        }
                        if (retries >= maxRetries ||
                                // Ignore if status can be retried. 400 is iffy.
                                (/*status != 400 &&*/ status != 429 && status < 500)) {

                            throw new RiotResponseException(String.format("Request failed after %d retries (%d).",
                                retries, r.getStatusCode()), r);
                        }
                        // Retry.
                        retries++; // Should be fine to not be synchronized, only one thread in this section.
                        RateLimitedRequester.executor.get().schedule(this, delay, TimeUnit.MILLISECONDS);
                    }).exceptionally(e -> {
                        completion.completeExceptionally(e);
                        return null;
                    });
                return;
            }
            RateLimitedRequester.executor.get().schedule(this, delay, TimeUnit.MILLISECONDS);
        }
    }
}
