/*
 * Copyright (C) 2014-2022 Neo Visionaries Inc.
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


import javax.jdo.JDOCanRetryException;
import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;


/**
 * Task executor.
 *
 * <style type="text/css">
 * pre.sample span.comment { color: darkgreen; }
 * </style>
 * <pre class="sample" style="border: solid 1px black; margin: 0.5em; padding: 0.5em;">
 * <span class="comment">// Persistence manager factory.</span>
 * {@link PersistenceManagerFactory} factory = ...;
 *
 * <span class="comment">// Create a task executor.</span>
 * {@link TaskExecutor} executor = new {@link #TaskExecutor(PersistenceManagerFactory) TaskExecutor}{@code (factory)};
 *
 * <span class="comment">// Create a task.</span>
 * {@link Task} task = new {@link TaskAdapter}() {
 *     &#x40;Override
 *     public void beforeTransactionBegin({@link PersistenceManager} manager, {@link Transaction} tx) {
 *         tx.{@link Transaction#setOptimistic(boolean) setOptimistic}(false);
 *     }
 *
 *     &#x40;Override
 *     public Object run({@link PersistenceManager} manager) {
 *         ......
 *     }
 * };
 *
 * <span class="comment">// Execute the task.</span>
 * executor.{@link #execute(Task, boolean, int) execute}{@code (task, true, 2)};
 * </pre>
 *
 * @since 1.2
 *
 * @author Takahiko Kawasaki
 */
public class TaskExecutor
{
    private PersistenceManagerFactory mFactory;
    private PersistenceManager mManager;


    /**
     * Constructor with a persistence manager factory.
     *
     * @param factory
     *         A persistence manager factory. Must not be {@code null}.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}.
     */
    public TaskExecutor(PersistenceManagerFactory factory)
    {
        // Check if the factory is null.
        if (factory == null)
        {
            // Factory must not be null.
            throw new IllegalArgumentException("factory is null.");
        }

        this.mFactory = factory;
    }


    /**
     * Constructor with a persistence manager.
     *
     * @param manager
     *         A persistence manager. Must not be {@code null}.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}.
     *
     * @since 1.19
     */
    public TaskExecutor(PersistenceManager manager)
    {
        // Check if the manager is null.
        if (manager == null)
        {
            // Manager must not be null.
            throw new IllegalArgumentException("manager is null.");
        }

        this.mManager = manager;
    }


    /**
     * Execute a task.
     *
     * <p>
     * This method is an alias of {@link #execute(Task, boolean, int)
     * execute(task, false, 0)}.
     * </p>
     *
     * @param task
     *         Task to be executed.
     *
     * @return
     *         The object returned from {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)}.
     *
     * @throws IllegalArgumentException
     *         {@code task} is {@code null}.
     *
     * @throws JDOException
     *         A persistence manager could not be obtained or the task failed.
     *         See the description of {@link #execute(Task, boolean, int)}
     *         for details.
     */
    public Object execute(Task task)
    {
        return execute(task, false, 0);
    }


    /**
     * Execute a task.
     *
     * <p>
     * This method is an alias of {@link #execute(Task, boolean, int)
     * execute(task, transaction, 0)}.
     * </p>
     *
     * @param task
     *         Task to be executed.
     *
     * @param transaction
     *         {@code true} to execute the task in a transaction.
     *
     * @return
     *         The object returned from {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)}.
     *
     * @throws IllegalArgumentException
     *         {@code task} is {@code null}.
     *
     * @throws JDOException
     *         A persistence manager could not be obtained or the task failed.
     *         See the description of {@link #execute(Task, boolean, int)}
     *         for details.
     */
    public Object execute(Task task, boolean transaction)
    {
        return execute(task, transaction, 0);
    }


    /**
     * Execute a task.
     *
     * <p>
     * This method is an alias of {@link #execute(Task, boolean, int)
     * execute(task, false, retryCount)}.
     * </p>
     *
     * @param task
     *         Task to be executed.
     *
     * @param retryCount
     *         Indicates how many times to retry the task. The task is retried
     *         only when {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)} threw a {@link JDOCanRetryException}.
     *         If an exception of other type was thrown, the task is not retried.
     *
     * @return
     *         The object returned from {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)}.
     *
     * @throws IllegalArgumentException
     *         {@code task} is {@code null}, or {@code retryCount} is less than 0.
     *
     * @throws JDOException
     *         A persistence manager could not be obtained or the task failed.
     *         See the description of {@link #execute(Task, boolean, int)}
     *         for details.
     *
     * @since 1.19
     */
    public Object execute(Task task, int retryCount)
    {
        return execute(task, false, retryCount);
    }


    /**
     * Execute a task.
     *
     * <p>
     * If this {@code TaskExecutor} instance was created by the constructor
     * {@link #TaskExecutor(PersistenceManagerFactory)}, a new instance of
     * {@link PersistenceManager} is created inside this {@code execute} method
     * by calling the {@link PersistenceManagerFactory#getPersistenceManager()
     * getPersistenceManager()} method of the {@link PersistenceManagerFactory}
     * instance that was passed via the constructor.
     * </p>
     *
     * <p>
     * On the other hand, if this {@code TaskExecutor} instance was created by
     * the constructor {@link #TaskExecutor(PersistenceManager)}, the
     * {@link PersistanceManager} instance passed via the constructor is used
     * inside this {@code execute} method.
     * </p>
     *
     * <p>
     * Note that if the current transaction of the {@link PersistenceManager}
     * instance that was passed via the constructor
     * {@link #TaskExecutor(PersistenceManager)} is already active, transaction
     * operations are NOT executed even if the {@code transaction} argument is
     * {@code true}.
     * </p>
     *
     * @param task
     *         Task to be executed. If the given instance implements the
     *         {@link TransactionAwareTask} interface, transaction-related
     *         callback methods of the interface will be called appropriately.
     *
     * @param transaction
     *         {@code true} to execute the task in a transaction.
     *
     * @param retryCount
     *         Indicates how many times to retry the task. The task is retried
     *         only when {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)} threw a {@link JDOCanRetryException}.
     *         If an exception of other type was thrown, the task is not retried.
     *
     * @return
     *         The object returned from {@code task.}{@link Task#run(PersistenceManager)
     *         run(PersistenceManager)}.
     *
     * @throws IllegalArgumentException
     *         {@code task} is {@code null}, or {@code retryCount} is less than 0.
     *
     * @throws JDOException
     *         There are several cases for this exception.
     *         <ul>
     *         <li>Failed to get a persistence manager from the persistence manager factory.
     *         <li>{@link PersistenceManagerFactory#getPersistenceManager()
     *             getPersistenceManager()} of the persistence manager factory
     *             returned {@code null}.
     *         <li>The task continued to throw {@link JDOCanRetryException} and
     *             exceeded the retry count.
     *         </ul>
     *
     * @throws RuntimeException
     *         The task continued to throw {@link JDOCanRetryException} and
     *         exceeded the retry count.
     */
    public Object execute(Task task, boolean transaction, int retryCount)
    {
        // Check if the task is null.
        if (task == null)
        {
            // Task must not be null.
            throw new IllegalArgumentException("task is null.");
        }

        // Check if the retry count is less than 0.
        if (retryCount < 0)
        {
            // Retry count must not be less than 0.
            throw new IllegalArgumentException("retryCount < 0");
        }

        // If this TaskExecutor instance was created by TaskExecutor(PersistenceManager),
        // the PersistenceManager instance given via the constructor is used.
        //
        // Otherwise, a new PersistenceManager instance is created here by calling the
        // getPersistenceManager() method of the PersistenceManagerFactory instance
        // that was given via TaskExecutor(PersistenceManagerFactory).
        PersistenceManager manager = (mManager != null) ? mManager : createManager();
        boolean managerCreated     = (mManager == null);

        try
        {
            // Execute the task.
            return doExecute(task, transaction, retryCount, manager);
        }
        finally
        {
            if (managerCreated)
            {
                // Close the persistence manager in any case.
                manager.close();
            }
        }
    }


    /**
     * Get the persistence manager factory that was given to the
     * {@link #TaskExecutor(PersistenceManagerFactory)} constructor.
     *
     * @return
     *         The instance of {@link PersistenceManagerFactory} that was given
     *         to the {@link #TaskExecutor(PersistenceManagerFactory)} constructor.
     *         {@code null} is returned if this {@code TaskExecutor} instance was
     *         created by other constructors.
     *
     * @since 1.19
     */
    public PersistenceManagerFactory getFactory()
    {
        return mFactory;
    }


    /**
     * Get the persistence manager that was given to the
     * {@link #TaskExecutor(PersistenceManager)} constructor.
     *
     * @return
     *         The instance of {@link PersistenceManager} that was given
     *         to the {@link #TaskExecutor(PersistenceManager)} constructor.
     *         {@code null} is returned if this {@code TaskExecutor} instance was
     *         created by other constructors.
     *
     * @since 1.19
     */
    public PersistenceManager getManager()
    {
        return mManager;
    }


    private PersistenceManager createManager()
    {
        PersistenceManager manager = null;

        try
        {
            // Get a persistence manager from the persistence manager factory.
            manager = mFactory.getPersistenceManager();
        }
        catch (JDOException e)
        {
            // getPersistenceManager() threw an exception.
            throw e;
        }
        catch (RuntimeException e)
        {
            // getPersistenceManager() threw an unexpected exception.
            throw new JDOException("getPersistenceManager() failed.", e);
        }

        if (manager == null)
        {
            // Failed to obtain a persistence manager.
            throw new JDOException("getPersistenceManager() returned null.");
        }

        return manager;
    }


    private Object doExecute(Task task, boolean transaction, int retryCount, PersistenceManager manager)
    {
        Transaction tx = manager.currentTransaction();

        // A flag that indicates whether to do transaction operations.
        // If the current transaction is already active, transaction
        // operations are not executed even if the 'transaction' flag
        // passed to the 'execute' method is true.
        boolean doTx = transaction && !tx.isActive();

        // If transaction is required.
        if (doTx)
        {
            // Begin a transaction.
            beginTransaction(manager, task, tx);
        }

        try
        {
            // Execute the task
            Object ret = doExecute(task, retryCount, manager);

            if (doTx)
            {
                // Commit the changes.
                commitTransaction(manager, task, tx);
            }

            return ret;
        }
        finally
        {
            // If transaction is required but not committed.
            if (doTx && tx.isActive())
            {
                // Revoke the changes.
                rollbackTransaction(manager, task, tx);
            }
        }
    }


    private static TransactionAwareTask cast(Task task)
    {
        if (task instanceof TransactionAwareTask)
        {
            return (TransactionAwareTask)task;
        }
        else
        {
            return null;
        }
    }


    private static void beginTransaction(PersistenceManager manager, Task task, Transaction tx)
    {
        // Cast by TransactionAwareTask if possible.
        TransactionAwareTask tat = cast(task);

        if (tat != null)
        {
            // Call a hook before beginning a transaction.
            tat.beforeTransactionBegin(manager, tx);
        }

        // Start a transaction.
        // begin() throws JDOUserException if something is wrong.
        tx.begin();

        if (tat != null)
        {
            // Call a hook after beginning a transaction.
            tat.afterTransactionBegin(manager, tx);
        }
    }


    private static void commitTransaction(PersistenceManager manager, Task task, Transaction tx)
    {
        // Cast by TransactionAwareTask if possible.
        TransactionAwareTask tat = cast(task);

        if (tat != null)
        {
            // Call a hook before committing a transaction.
            tat.beforeTransactionCommit(manager, tx);
        }

        // Commit the changes.
        tx.commit();

        if (tat != null)
        {
            // Call a hook after committing a transaction.
            tat.afterTransactionCommit(manager, tx);
        }
    }


    private static void rollbackTransaction(PersistenceManager manager, Task task, Transaction tx)
    {
        // Cast by TransactionAwareTask if possible.
        TransactionAwareTask tat = cast(task);

        if (tat != null)
        {
            // Call a hook before rollbacking a transaction.
            tat.beforeTransactionRollback(manager, tx);
        }

        // Revoke the changes.
        tx.rollback();

        if (tat != null)
        {
            // Call a hook after rollbacking a transaction.
            tat.afterTransactionRollback(manager, tx);
        }
    }


    private Object doExecute(Task task, int retryCount, PersistenceManager manager)
    {
        // The exception that task.run(manager) threw.
        JDOCanRetryException lastException = null;

        // Retries up to the retry count at a maximum.
        for (int count = 0; count <= retryCount; ++count)
        {
            try
            {
                // Execute the task.
                return task.run(manager);
            }
            catch (JDOCanRetryException e)
            {
                // The task threw an exception, but it may succeed when retried.
                lastException = e;
            }
        }

        // The task continued to throw JDOCanRetryException and
        // exceeded the retry count.
        throw lastException;
    }
}
