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


import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;


/**
 * DAO based on JDO.
 *
 * <style type="text/css">
 * pre.sample span.comment { color: #009900; }
 * </style>
 * <pre class="sample" style="border: solid 1px black; margin: 0.5em; padding: 0.5em;">
 * <span class="comment">// Set up a shared persistence manager factory.</span>
 * JdoDao.{@link #setupSharedPersistenceManagerFactory(String)
 * setupSharedPersistenceManagerFactory}("transactions-optional");
 *
 * <span class="comment">// Create a DAO with an entity class.</span>
 * JdoDao&lt;Customer&gt; dao = new JdoDao&lt;Customer&gt;(Customer.class);
 *
 * <span class="comment">// or</span>
 * JdoDao&lt;Customer&gt; dao = JdoDao.{@link #create(Class) create}(Customer.class);
 *
 * <span class="comment">// Use the DAO.</span>
 * Customer customer = dao.{@link #getById(Object) getById}(customerId);
 * </pre>
 *
 * @param <TEntity>
 */
public class JdoDao<TEntity>
{
    private static PersistenceManagerFactory sharedPersistenceManagerFactory;
    private final Class<TEntity> entityClass;
    private PersistenceManagerFactory persistenceManagerFactory;


    /**
     * Get the persistence manager factory that has been set by
     * {@link #setSharedPersistenceManagerFactory(PersistenceManagerFactory)}.
     *
     * @return
     *         The shared persistence manager factory.
     */
    public static PersistenceManagerFactory getSharedPersistenceManagerFactory()
    {
        return sharedPersistenceManagerFactory;
    }


    /**
     * Set a persistence manager factory that is shared as fail-safe for
     * {@code JdoDao} instances which do not have a persistence manager
     * factory of their own.
     *
     * @param factory
     *         A persistence manager factory.
     */
    public static void setSharedPersistenceManagerFactory(PersistenceManagerFactory factory)
    {
        sharedPersistenceManagerFactory = factory;
    }


    /**
     * Set up a shared persistence manager factor. This method calls
     * {@link JDOHelper#getPersistenceManagerFactory(String)
     * JDOHelper.getPersistenceManagerFactory}{@code (name)} and sets
     * the returned value as the shared persistence manager factory.
     *
     * <pre style="border: solid 1px black; margin: 0.5em; padding: 0.5em;">
     * JdoDao.{@link #setupSharedPersistenceManagerFactory(String)
     * setupSharedPersistenceManagerFactory}("transactions-optional");</pre>
     *
     * @param name
     */
    public static void setupSharedPersistenceManagerFactory(String name)
    {
        sharedPersistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(name);
    }


    /**
     * Create a DAO for the specified entity.
     *
     * <p>
     * This method just does the following:
     * </p>
     *
     * <blockquote><pre>
     * return new JdoDao&lt;TEntity&gt;(entityClass);
     * </pre></blockquote>
     *
     * @param entityClass
     *
     * @return
     *         A new DAO.
     *
     * @since 1.1
     */
    public static <TEntity> JdoDao<TEntity> create(Class<TEntity> entityClass)
    {
        return new JdoDao<TEntity>(entityClass);
    }


    /**
     * Constructor.
     *
     * @param entityClass
     *         The class of entities handled by this DAO.
     */
    public JdoDao(Class<TEntity> entityClass)
    {
        this(entityClass, null);
    }


    /**
     * Constructor with a persistence manager factory for this instance.
     *
     * @param entityClass
     *         The class of entities handled by this DAO.
     *
     * @param factory
     *         A persistence manager factory.
     */
    public JdoDao(Class<TEntity> entityClass, PersistenceManagerFactory factory)
    {
        checkNonNull(entityClass, "entityClass");

        this.entityClass = entityClass;
        this.persistenceManagerFactory = factory;
    }


    /**
     * Get the persistence manager factory for this instance.
     *
     * @return
     *         The persistence manager factory that has been set by
     *         {@link #setPersistenceManagerFactory(PersistenceManagerFactory)}.
     */
    public PersistenceManagerFactory getPersistenceManagerFactory()
    {
         return persistenceManagerFactory;
    }


    /**
     * Set a persistence manager factory for this instance.
     *
     * @param factory
     *         A persistence manager factory to set.
     *
     * @return
     *         {@code this} object.
     */
    public JdoDao<TEntity> setPersistenceManagerFactory(PersistenceManagerFactory factory)
    {
        this.persistenceManagerFactory = factory;

        return this;
    }


    /**
     * Get a persistence manager, which is obtained from the persistence
     * manager factory which is set to this JdoDao instance, or from the
     * shared persistence manager factory when no persistence manager
     * factory is set to this instance.
     *
     * @return
     *         A persistence manager, or {@code null} if no persistence
     *         manager factory is set.
     */
    public PersistenceManager getPersistenceManager()
    {
        PersistenceManagerFactory factory = getPersistenceManagerFactory();

        if (factory == null)
        {
            factory = sharedPersistenceManagerFactory;
        }

        if (factory == null)
        {
            return null;
        }

        return factory.getPersistenceManager();
    }


    private PersistenceManager getPersistenceManagerAndCheck()
    {
        PersistenceManager manager = getPersistenceManager();

        if (manager == null)
        {
            throw new IllegalStateException("Persistence Manager is not available.");
        }

        return manager;
    }


    private void checkNonNull(Object object, String name)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(name + " is null.");
        }
    }


    /**
     * Delete an entity. This method calls
     * {@link PersistenceManager#deletePersistent(Object)}.
     *
     * @param entity
     *         The entity to delete.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         Persistent Manager is not available.
     */
    public void delete(TEntity entity)
    {
        checkNonNull(entity, "entity");

        PersistenceManager manager = getPersistenceManagerAndCheck();

        try
        {
            manager.deletePersistent(entity);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete an entity. This method calls
     * {@link PersistenceManager#getObjectById(Class, Object)}
     * to find the entity and then calls
     * {@link PersistenceManager#deletePersistent(Object)}
     * to delete the entity.
     *
     * @param id
     *         The ID of the entity.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         Persistent Manager is not available.
     */
    public void deleteById(Object id)
    {
        checkNonNull(id, "id");

        PersistenceManager manager = getPersistenceManagerAndCheck();

        try
        {
            TEntity entity = manager.getObjectById(entityClass, id);

            if (entity != null)
            {
                manager.deletePersistent(entity);
            }
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get an entity. This method calls
     * {@link PersistenceManager#getObjectById(Class, Object)}.
     *
     * @param id
     *         The ID of the entity.
     *
     * @return
     *         The entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         Persistent Manager is not available.
     */
    public TEntity getById(Object id)
    {
        checkNonNull(id, "id");

        PersistenceManager manager = getPersistenceManagerAndCheck();

        try
        {
            return manager.getObjectById(entityClass, id);
        }
        catch (JDOObjectNotFoundException e)
        {
            // Not found.
            return null;
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Make an entity persistent. This method calls
     * {@link PersistenceManager#makePersistent(Object)}.
     *
     * @param entity
     *         An entity to be persistent.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         Persistent Manager is not available.
     *
     * @throws JDOUserException
     */
    public void put(TEntity entity)
    {
        checkNonNull(entity, "entity");

        PersistenceManager manager = getPersistenceManagerAndCheck();

        try
        {
            manager.makePersistent(entity);
        }
        finally
        {
            manager.close();
        }
    }
}
