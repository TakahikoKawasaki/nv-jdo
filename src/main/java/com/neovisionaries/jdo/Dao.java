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


import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;


/**
 * DAO based on JDO.
 *
 * <style type="text/css">
 * pre.sample span.comment { color: darkgreen; }
 * </style>
 * <pre class="sample" style="border: solid 1px black; margin: 0.5em; padding: 0.5em;">
 * <span class="comment">// Persistence manager factory.</span>
 * PersistenceManagerFactory factory = ...;
 *
 * <span class="comment">// Create a DAO with an entity class and the persistence manager factory.</span>
 * Dao&lt;Customer&gt; dao = new Dao&lt;Customer&gt;(Customer.class, factory);
 *
 * <span class="comment">// Or using a static method.</span>
 * Dao&lt;Customer&gt; dao = Dao.{@link #create(Class) create}(Customer.class, factory);
 *
 * <span class="comment">// Use the DAO.</span>
 * Customer customer = dao.{@link #getById(Object) getById}(customerId);
 * </pre>
 *
 * @param <TEntity>
 *
 * @since 1.2
 *
 * @author Takahiko Kawasaki
 */
public class Dao<TEntity>
{
    private final Class<TEntity> entityClass;
    private PersistenceManagerFactory factory;


    /**
     * Create a DAO for the specified entity.
     *
     * <p>
     * This method just does the following:
     * </p>
     *
     * <blockquote><pre>
     * return new Dao&lt;TEntity&gt;(entityClass);
     * </pre></blockquote>
     *
     * @param entityClass
     *         Entity class.
     *
     * @return
     *         A new DAO.
     */
    public static <TEntity> Dao<TEntity> create(Class<TEntity> entityClass)
    {
        return new Dao<TEntity>(entityClass);
    }


    /**
     * Create a DAO for the specified entity.
     *
     * <p>
     * This method just does the following:
     * </p>
     *
     * <blockquote><pre>
     * return new Dao&lt;TEntity&gt;(entityClass, factory);
     * </pre></blockquote>
     *
     * @param entityClass
     *         Entity class.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @return
     *         A new DAO.
     */
    public static <TEntity> Dao<TEntity> create(Class<TEntity> entityClass, PersistenceManagerFactory factory)
    {
        return new Dao<TEntity>(entityClass, factory);
    }


    /**
     * Constructor. This constructor is an alias of
     * {@link #Dao(Class, PersistenceManagerFactory)
     * Dao(entityClass, null)}.
     *
     * @param entityClass
     *         The class of entities handled by this DAO.
     */
    public Dao(Class<TEntity> entityClass)
    {
        this(entityClass, null);
    }


    /**
     * Constructor.
     *
     * @param entityClass
     *         The class of entities handled by this DAO.
     *
     * @param factory
     *         A persistence manager factory.
     */
    public Dao(Class<TEntity> entityClass, PersistenceManagerFactory factory)
    {
        this.entityClass = entityClass;
        this.factory     = factory;
    }


    /**
     * Check if the given object is {@code null}.
     *
     * @param object
     *         Object to be checked.
     *
     * @param name
     *         Name of the object.
     *
     * @throws IllegalArgumentException
     *         The object is {@code null}.
     */
    private void checkNonNull(Object object, String name) throws IllegalArgumentException
    {
        if (object == null)
        {
            throw new IllegalArgumentException(name + " is null.");
        }
    }


    /**
     * Check if a persistence manager factory is set.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set.
     */
    private void checkFactory() throws IllegalStateException
    {
        if (factory == null)
        {
            throw new IllegalStateException("PersistenceManagerFactory is not set.");
        }
    }


    /**
     * Create a persistence manager by calling
     * {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     * getPersistenceManagerFactory()}.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @return
     *         A persistence manager.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManagerFactory()} threw an exception. Or,
     *         {@code getPersistenceManager()} returned {@code null}.
     */
    private PersistenceManager createManager(PersistenceManagerFactory factory)
            throws IllegalArgumentException
    {
        PersistenceManager manager = null;

        try
        {
            manager = factory.getPersistenceManager();
        }
        catch (Throwable t)
        {
            throw new IllegalStateException("getPersistenceManager() failed.", t);
        }

        if (manager == null)
        {
            throw new IllegalStateException("getPersistenceManager() returned null.");
        }

        return manager;
    }


    /**
     * Get the persistence manager factory that this instance currently holds.
     *
     * @return
     *         Persistence manager factory.
     */
    public PersistenceManagerFactory getPersistenceManagerFactory()
    {
        return factory;
    }


    /**
     * Set a persistence manager factory.
     *
     * @param factory
     *         Persistence manager factory.
     *
     * @return
     *         {@code this} object.
     */
    public Dao<TEntity> setPersistenceManagerFactory(PersistenceManagerFactory factory)
    {
        this.factory = factory;

        return this;
    }


    /**
     * Delete an entity. A persistence manager factory must be set
     * before this method is called.
     *
     * @param entity
     *         An entity to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void delete(TEntity entity)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entity, "entity");
        checkFactory();

        delete(entity, factory);
    }


    /**
     * Delete an entity.
     *
     * @param entity
     *         An entity to be deleted.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}, or {@code factory} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void delete(TEntity entity, PersistenceManagerFactory factory)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entity,  "entity");
        checkNonNull(factory, "factory");

        PersistenceManager manager = createManager(factory);

        try
        {
            delete(entity, manager);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete an entity.
     *
     * @param entity
     *         An entity to be deleted.
     *
     * @param manager
     *         A persistence manager.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}, or {@code manager} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void delete(TEntity entity, PersistenceManager manager)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(entity,  "entity");
        checkNonNull(manager, "manager");

        manager.deletePersistent(entity);
    }


    /**
     * Delete an entity. If there is no entity having the ID,
     * no change is made. A persistence manager factory must
     * be set before this method is called.
     *
     * @param id
     *         Entity ID.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed). Or,
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void deleteById(Object id)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(id, "id");
        checkFactory();

        deleteById(id, factory);
    }


    /**
     * Delete an entity by ID. If there is no entity having the ID,
     * no change is made.
     *
     * @param id
     *         Entity ID.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}, or {@code factory} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed. Or,
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void deleteById(Object id, PersistenceManagerFactory factory)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(id,      "id");
        checkNonNull(factory, "factory");

        PersistenceManager manager = createManager(factory);

        try
        {
            deleteById(id, manager);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete an entity by ID. If there is no entity having the ID,
     * no change is made.
     *
     * @param id
     *         Entity ID.
     *
     * @param manager
     *         A persistence manager.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}, or {@code manager} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     */
    public void deleteById(Object id, PersistenceManager manager)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(id,      "id");
        checkNonNull(manager, "manager");

        TEntity entity = getById(id, manager);

        if (entity != null)
        {
            delete(entity, manager);
        }
    }


    /**
     * Get an entity having the specified ID.
     * A persistence manager factory must be set
     * before this method is called.
     *
     * @param id
     *         Entity ID.
     *
     * @return
     *         An entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set.
     *
     */
    public TEntity getById(Object id)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(id, "id");
        checkFactory();

        return getById(id, factory);
    }


    /**
     * Get an entity having the specified ID.
     *
     * @param id
     *         Entity ID.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @return
     *         An entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}, or {@code factory} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed. Or,
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     */
    public TEntity getById(Object id, PersistenceManagerFactory factory)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(id,      "id");
        checkNonNull(factory, "factory");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getById(id, manager);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get an entity having the specified ID.
     *
     * @param id
     *         Entity ID.
     *
     * @param manager
     *         A persistence manager.
     *
     * @return
     *         An entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code id} is {@code null}, or {@code manager} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     */
    public TEntity getById(Object id, PersistenceManager manager)
            throws IllegalArgumentException , IllegalStateException
    {
        checkNonNull(id,      "id");
        checkNonNull(manager, "manager");

        try
        {
            return manager.getObjectById(entityClass, id);
        }
        catch (JDOObjectNotFoundException e)
        {
            // Not found.
            return null;
        }
        catch (Throwable t)
        {
            throw new IllegalStateException("getObjectById() failed.", t);
        }
    }


    /**
     * Make an entity persistent.
     * A persistence manager factory must be set
     * before this method is called.
     *
     * @param entity
     *         An entity to be made persistent.
     *
     * @throws IllegalArgumentException
     *         {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         Persistence manager factory is not set.
     *
     * @throws JDOUserException
     */
    public void put(TEntity entity)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entity, "entity");
        checkFactory();

        // TODO
    }
}
