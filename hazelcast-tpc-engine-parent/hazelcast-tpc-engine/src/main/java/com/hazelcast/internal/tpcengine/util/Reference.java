/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.tpcengine.util;

/**
 * A general purpose reference to some object that is comparable to an
 * {@link java.util.concurrent.atomic.AtomicReference} but has plain
 * load/store semantics.
 * <p/>
 * The primary purpose of this Reference is to be able to pass a reference to
 * a callee and allow the callee to modify the value of the reference.
 *
 * @param <E>
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public class Reference<E> {
    public E value;

    public Reference() {
    }

    public Reference(E value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Reference{value=" + value + '}';
    }
}
