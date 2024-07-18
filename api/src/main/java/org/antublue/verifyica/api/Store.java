/*
 * Copyright (C) 2024 The Verifyica project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.antublue.verifyica.api;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.junit.platform.commons.util.Preconditions;

/** Class to implement Store */
@SuppressWarnings("unchecked")
public class Store {

    private final ReadWriteLock readWriteLock;
    private final Map<Object, Object> map;

    /** Constructor */
    public Store() {
        readWriteLock = new ReentrantReadWriteLock(true);
        map = new TreeMap<>();
    }

    /**
     * Method to put an key / value into the Store
     *
     * @param key key
     * @param value value
     * @return the Store
     */
    public Store put(Object key, Object value) {
        Preconditions.notNull(key, "key is null");

        try {
            readWriteLock.writeLock().lock();

            if (value == null) {
                map.remove(key);
            } else {
                map.put(key, value);
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get a value from the Store
     *
     * @param key key
     * @return the value
     * @param <T> type
     */
    public <T> T get(Object key) {
        Preconditions.notNull(key, "key is null");

        try {
            readWriteLock.readLock().lock();
            return (T) map.get(key);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Method to get value from the Store
     *
     * @param key key
     * @param type type
     * @return the value
     * @param <T> type
     */
    public <T> T get(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        try {
            readWriteLock.readLock().lock();
            return type.cast(map.get(key));
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Method to get a value from the Store, throwing an Exception if null
     *
     * @param key key
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
    public <T> T getOrThrow(Object key, Supplier<? extends RuntimeException> supplier) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(supplier, "supplier is null");

        T t = get(key);
        if (t == null) {
            throw supplier.get();
        } else {
            return t;
        }
    }

    /**
     * Method to get a value from the Store, throwing an Exception if null
     *
     * @param key key
     * @param type type
     * @param supplier supplier
     * @return the Object
     * @param <T> type
     */
    public <T> T getOrThrow(
            Object key, Class<T> type, Supplier<? extends RuntimeException> supplier) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");
        Preconditions.notNull(supplier, "supplier is null");

        T t = get(key, type);
        if (t == null) {
            throw supplier.get();
        } else {
            return t;
        }
    }

    /**
     * Method to remove key /value from the Store
     *
     * @param key key
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key) {
        Preconditions.notNull(key, "key is null");

        try {
            readWriteLock.writeLock().lock();
            return (T) map.remove(key);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to remove a key / value from the Store
     *
     * @param key key
     * @param type type
     * @return the existing Object
     * @param <T> type
     */
    public <T> T remove(Object key, Class<T> type) {
        Preconditions.notNull(key, "key is null");
        Preconditions.notNull(type, "type is null");

        try {
            readWriteLock.writeLock().lock();
            return type.cast(map.remove(key));
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * Method to clear the Store
     *
     * @return the Store
     */
    public Store clear() {
        try {
            readWriteLock.writeLock().lock();

            for (Object value : map.values()) {
                if (value instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) value).close();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            map.clear();
        } finally {
            readWriteLock.writeLock().unlock();
        }

        return this;
    }

    /**
     * Method to get the Store size
     *
     * @return the size
     */
    public int size() {
        try {
            readWriteLock.readLock().lock();
            return map.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    /**
     * Method to merge a Store into this Store
     *
     * @param store store
     * @return the Store
     */
    public Store merge(Store store) {
        Preconditions.notNull(store, "store is null");

        if (store.size() > 0) {
            try {
                store.getLock().readLock().lock();
                getLock().writeLock().lock();

                map.putAll(store.map);
            } finally {
                getLock().writeLock().unlock();
                store.getLock().readLock().unlock();
            }
        }

        return this;
    }

    /**
     * Method to get the Store lock
     *
     * @return the Store lock
     */
    public ReadWriteLock getLock() {
        return readWriteLock;
    }

    @Override
    public String toString() {
        try {
            readWriteLock.readLock().lock();
            return map.toString();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(map, store.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
