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
 * Empty implementation of the {@link TransactionAwareTask} interface.
 *
 * @since 1.16
 *
 * @author Takahiko Kawasaki
 */
public class TaskAdapter implements TransactionAwareTask
{
    public Object run(PersistenceManager manager)
    {
        return null;
    }


    public void beforeTransactionBegin(PersistenceManager manager, Transaction tx)
    {
    }


    public void afterTransactionBegin(PersistenceManager manager, Transaction tx)
    {
    }


    public void beforeTransactionCommit(PersistenceManager manager, Transaction tx)
    {
    }


    public void afterTransactionCommit(PersistenceManager manager, Transaction tx)
    {
    }


    public void beforeTransactionRollback(PersistenceManager manager, Transaction tx)
    {
    }


    public void afterTransactionRollback(PersistenceManager manager, Transaction tx)
    {
    }
}
