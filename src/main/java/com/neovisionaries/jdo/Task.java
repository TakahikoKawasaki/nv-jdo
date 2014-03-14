/*
 * Copyright (C) 2014 Neo Visionaries Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.neovisionaries.jdo;


import javax.jdo.PersistenceManager;


/**
 * Task executed by {@link TaskExecutor}.
 *
 * @since 1.2
 *
 * @author Takahiko Kawasaki
 */
public interface Task
{
    /**
     * Task body.
     *
     * @param manager
     *         A persistence manager.
     *
     * @return
     *         Any arbitrary object. The object returned by
     *         this method is returned from {@link TaskExecutor}'s
     *         {@code execute} methods. {@code TaskExecutor} does
     *         not care about what the object is. Even {@code null}
     *         is OK.
     */
    public Object run(PersistenceManager manager);
}
