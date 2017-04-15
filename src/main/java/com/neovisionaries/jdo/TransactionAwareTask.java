/*
 * Copyright (C) 2017 Neo Visionaries Inc.
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
import javax.jdo.Transaction;


/**
 * Task executed by {@link TaskExecutor}.
 *
 * <p>
 * If a task given to {@code TaskExecutor} implements this interface and
 * if a {@code execute()} method of {@code TaskExecutor} (e.g. {@link
 * TaskExecutor#execute(Task, boolean) execute(Task task, boolean transaction)})
 * is called with the {@code transaction} parameter {@code true}, methods
 * defined in this interface are called accordingly.
 * </p>
 *
 * <p>
 * Do not expect that either {@link #beforeTransactionCommit(PersistenceManager, Transaction)
 * beforeTransactionCommit()} or {@link #beforeTransactionRollback(PersistenceManager, Transaction)
 * beforeTransactionRollback()} is always called. Read the source code of <code><a href=
 * "https://github.com/TakahikoKawasaki/nv-jdo/blob/master/src/main/java/com/neovisionaries/jdo/TaskExecutor.java"
 * >TaskExecutor</a></code>, and you will find there are cases where neither is called.
 * </p>
 *
 * @since 1.16
 *
 * @author Takahiko Kawasaki
 */
public interface TransactionAwareTask extends Task
{
    /**
     * A hook called before {@link Transaction#begin()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void beforeTransactionBegin(PersistenceManager manager, Transaction tx);


    /**
     * A hook called after {@link Transaction#begin()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void afterTransactionBegin(PersistenceManager manager, Transaction tx);


    /**
     * A hook called before {@link Transaction#commit()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void beforeTransactionCommit(PersistenceManager manager, Transaction tx);


    /**
     * A hook called after {@link Transaction#commit()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void afterTransactionCommit(PersistenceManager manager, Transaction tx);


    /**
     * A hook called before {@link Transaction#rollback()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void beforeTransactionRollback(PersistenceManager manager, Transaction tx);


    /**
     * A hook called after {@link Transaction#rollback()} is executed.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param tx
     *         The current transaction.
     */
    public void afterTransactionRollback(PersistenceManager manager, Transaction tx);
}
