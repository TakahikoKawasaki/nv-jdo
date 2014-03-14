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
 * PersistenceManagerFactory factory = ...;
 *
 * <span class="comment">// Create a task executor.</span>
 * TaskExecutor executor = new {@link #TaskExecutor(PersistenceManagerFactory) TaskExecutor(factory)};
 *
 * <span class="comment">// Create a task.</span>
 * Task task = new Task() {
 *     &#x40;Override
 *     public Object run(PersistenceManager manager) {
 *         ......
 *     }
 * };
 *
 * <span class="comment">// Execute the task.</span>
 * executor.{@link #execute(Task, boolean, int) execute(task, true, 2)};
 * </pre>
 *
 * @since 1.2
 *
 * @author Takahiko Kawasaki
 */
public class TaskExecutor
{
    private final PersistenceManagerFactory factory;


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

        this.factory = factory;
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
     * @param task
     *         Task to be executed.
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
     *         <li>The task threw a {@code RuntimeException} other than
     *             {@link JDOCanRetryException}.
     *         <li>The task continued to throw {@link JDOCanRetryException} and
     *             exceeded the retry count.
     *         </ul>
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

        PersistenceManager manager = null;

        try
        {
            // Get a persistence manager from the persistence manager factory.
            manager = factory.getPersistenceManager();
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

        try
        {
            // Execute the task.
            return doExecute(task, transaction, retryCount, manager);
        }
        finally
        {
            // Close the persistence manager in any case.
            manager.close();
        }
    }


    private Object doExecute(Task task, boolean transaction, int retryCount, PersistenceManager manager)
    {
        Transaction tx = null;

        // If transaction is required.
        if (transaction)
        {
            // Start a transaction. begin() throws JDOUserException
            // if something is wrong.
            tx = manager.currentTransaction();
            tx.begin();
        }

        try
        {
            // Execute the task
            Object ret = doExecute(task, retryCount, manager);

            if (tx != null)
            {
                // Commit the changes, if any.
                tx.commit();
            }

            return ret;
        }
        finally
        {
            // If transaction is required but not committed.
            if (tx != null && tx.isActive())
            {
                // Revoke the changes.
                tx.rollback();
            }
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
        throw new JDOException("task failed.", lastException);
    }
}
