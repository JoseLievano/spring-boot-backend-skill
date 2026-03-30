package com.authServer.shared.functional;

/**
 * Functional interface similar to Supplier but allows checked exceptions.
 *
 * This interface is designed for scenarios where the supplied value computation
 * may throw checked exceptions, particularly useful in upload operations or
 * other I/O-intensive tasks.
 *
 * @param <T> the type of results supplied by this supplier
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    /**
     * Gets a result, potentially throwing a checked exception.
     *
     * @return a result
     * @throws Exception if unable to supply result
     */
    T get() throws Exception;
}