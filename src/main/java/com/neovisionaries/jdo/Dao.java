/*
 * Copyright (C) 2014-2015 Neo Visionaries Inc.
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


import java.util.Collection;
import java.util.List;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;


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
 *         The class that represents the entity.
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

        delete(factory, entity);
    }


    /**
     * Delete an entity.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param entity
     *         An entity to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     *
     * @since 1.6
     */
    public void delete(PersistenceManagerFactory factory, TEntity entity)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory, "factory");
        checkNonNull(entity,  "entity");

        PersistenceManager manager = createManager(factory);

        try
        {
            delete(manager, entity);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete an entity.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entity
     *         An entity to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entity} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     *
     * @since 1.6
     */
    public void delete(PersistenceManager manager, TEntity entity)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(manager, "manager");
        checkNonNull(entity,  "entity");

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

        deleteById(factory, id);
    }


    /**
     * Delete an entity by ID. If there is no entity having the ID,
     * no change is made.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param id
     *         Entity ID.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code id} is {@code null}.
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
     *
     * @since 1.6
     */
    public void deleteById(PersistenceManagerFactory factory, Object id)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory, "factory");
        checkNonNull(id,      "id");

        PersistenceManager manager = createManager(factory);

        try
        {
            deleteById(manager, id);
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
     * @param manager
     *         A persistence manager.
     *
     * @param id
     *         Entity ID.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistent(Object)
     *         deletePersistent(entity)} failed.
     *
     * @since 1.6
     */
    public void deleteById(PersistenceManager manager, Object id)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(manager, "manager");
        checkNonNull(id,      "id");

        TEntity entity = getById(manager, id);

        if (entity != null)
        {
            delete(manager, entity);
        }
    }


    /**
     * Delete entities. A persistence manager factory must be set
     * before this method is called.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code entities} is {@code null}.
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
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Collection)
     *         deletePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(Collection<TEntity> entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entities, "entities");
        checkFactory();

        deleteAll(factory, entities);
    }


    /**
     * Delete entities.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Collection)
     *         deletePersistentAll(Collection)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(PersistenceManagerFactory factory, Collection<TEntity> entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory,  "factory");
        checkNonNull(entities, "entities");

        PersistenceManager manager = createManager(factory);

        try
        {
            deleteAll(manager, entities);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete entities.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Collection)
     *         deletePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(PersistenceManager manager, Collection<TEntity> entities)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(manager,  "manager");
        checkNonNull(entities, "entities");

        manager.deletePersistentAll(entities);
    }


    /**
     * Delete entities. A persistence manager factory must be set
     * before this method is called.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code entities} is {@code null}.
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
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Object...)
     *         deletePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(TEntity... entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entities, "entities");
        checkFactory();

        deleteAll(factory, entities);
    }


    /**
     * Delete entities.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Object...)
     *         deletePersistentAll(Collection)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(PersistenceManagerFactory factory, TEntity... entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory,  "factory");
        checkNonNull(entities, "entities");

        PersistenceManager manager = createManager(factory);

        try
        {
            deleteAll(manager, entities);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete entities.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entities
     *         Entities to be deleted.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#deletePersistentAll(Object...)
     *         deletePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public void deleteAll(PersistenceManager manager, TEntity... entities)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(manager,  "manager");
        checkNonNull(entities, "entities");

        manager.deletePersistentAll(entities);
    }


    /**
     * Delete entities using JDOQL.
     * A persistence manager factory must be set before this method is called.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         The number of deleted instances. This is the value
     *         returned from {@link Query#deletePersistentAll(Object...)}
     *         method.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.10
     */
    public long deleteByQuery(String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(jdoql, "jdoql");
        checkFactory();

        return deleteByQuery(factory, jdoql, parameters);
    }


    /**
     * Delete entities using JDOQL.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         The number of deleted instances. This is the value
     *         returned from {@link Query#deletePersistentAll(Object...)}
     *         method.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.10
     */
    public long deleteByQuery(PersistenceManagerFactory factory, String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(jdoql,   "jdoql");

        PersistenceManager manager = createManager(factory);

        try
        {
            return deleteByQuery(manager, jdoql, parameters);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Delete entities using JDOQL.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         The number of deleted instances. This is the value
     *         returned from {@link Query#deletePersistentAll(Object...)}
     *         method.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @since 1.10
     */
    public long deleteByQuery(PersistenceManager manager, String jdoql, Object... parameters)
            throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");
        checkNonNull(jdoql,   "jdoql");

        Query query = manager.newQuery(jdoql);
        query.setClass(entityClass);

        return query.deletePersistentAll(parameters);
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
     */
    public TEntity getById(Object id)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(id, "id");
        checkFactory();

        return getById(factory, id);
    }


    /**
     * Get an entity having the specified ID.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param id
     *         Entity ID.
     *
     * @return
     *         An entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed. Or,
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @since 1.6
     */
    public TEntity getById(PersistenceManagerFactory factory, Object id)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(id,      "id");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getById(manager, id);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get an entity having the specified ID.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param id
     *         Entity ID.
     *
     * @return
     *         An entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code id} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code PersistenceManager.}{@link PersistenceManager#getObjectById(Class, Object)
     *         getObjectById(entityClass, id)} threw an exception other than
     *         {@link JDOObjectNotFoundException}.
     *
     * @since 1.6
     */
    public TEntity getById(PersistenceManager manager, Object id)
            throws IllegalArgumentException , IllegalStateException
    {
        checkNonNull(manager, "manager");
        checkNonNull(id,      "id");

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
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistent(Object)
     *         makePersistent(entity)} failed.
     */
    public void put(TEntity entity)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entity, "entity");
        checkFactory();

        put(factory, entity);
    }


    /**
     * Make an entity persistent.
     *
     * @param factory
     *         An persistence manager factory.
     *
     * @param entity
     *         An entity to be made persistent.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entity} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistent(Object)
     *         makePersistent(entity)} failed.
     *
     * @since 1.6
     */
    public void put(PersistenceManagerFactory factory, TEntity entity)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory, "factory");
        checkNonNull(entity,  "entity");

        PersistenceManager manager = createManager(factory);

        try
        {
            put(manager, entity);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Make an entity persistent.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entity
     *         An entity to be made persistent.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entity} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code manager.}{@link PersistenceManager#makePersistent(Object)
     *         makePersistent(entity)} failed.
     *
     * @since 1.6
     */
    public void put(PersistenceManager manager, TEntity entity)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(manager, "manager");
        checkNonNull(entity,  "entity");

        manager.makePersistent(entity);
    }


    /**
     * Make entities persistent.
     * A persistence manager factory must be set
     * before this method is called.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instance for parameters in the transient or persistent
     *         state, or the corresponding persistent instance for detached parameter
     *         instances, with an iteration in the same order as in the parameter
     *         collection. (= The value returned from {@link
     *         PersistenceManager#makePersistentAll(Collection)}).
     *
     * @throws IllegalArgumentException
     *         {@code entities} is {@code null}.
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
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistentAll(Collection)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public Collection<TEntity> putAll(Collection<TEntity> entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(entities, "entities");
        checkFactory();

        return putAll(factory, entities);
    }


    /**
     * Make entities persistent.
     *
     * @param factory
     *         An persistence manager factory.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instance for parameters in the transient or persistent
     *         state, or the corresponding persistent instance for detached parameter
     *         instances, with an iteration in the same order as in the parameter
     *         collection. (= The value returned from {@link
     *         PersistenceManager#makePersistentAll(Collection)}).
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistentAll(Collection)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public Collection<TEntity> putAll(PersistenceManagerFactory factory, Collection<TEntity> entities)
            throws IllegalArgumentException, IllegalStateException, JDOUserException
    {
        checkNonNull(factory,  "factory");
        checkNonNull(entities, "entities");

        PersistenceManager manager = createManager(factory);

        try
        {
            return putAll(manager, entities);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Make entities persistent.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instance for parameters in the transient or persistent
     *         state, or the corresponding persistent instance for detached parameter
     *         instances, with an iteration in the same order as in the parameter
     *         collection. (= The value returned from {@link
     *         PersistenceManager#makePersistentAll(Collection)}).
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code manager.}{@link PersistenceManager#makePersistentAll(Collection)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public Collection<TEntity> putAll(PersistenceManager manager, Collection<TEntity> entities)
            throws IllegalArgumentException, JDOUserException
    {
        checkNonNull(manager,  "manager");
        checkNonNull(entities, "entities");

        return manager.makePersistentAll(entities);
    }


    /**
     * Make entities persistent.
     * A persistence manager factory must be set
     * before this method is called.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instances for parameters in the transient or persistent state,
     *         or the corresponding persistent instance for detached parameter instances,
     *         in the same order as in the parameter array. (= The value returned from
     *         {@link PersistenceManager#makePersistentAll(Object...)}).
     *
     * @throws IllegalArgumentException
     *         {@code entities} is {@code null}.
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
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistentAll(Object...)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public TEntity[] putAll(TEntity... entities)
    {
        checkNonNull(entities, "entities");
        checkFactory();

        return putAll(factory, entities);
    }


    /**
     * Make entities persistent.
     *
     * @param factory
     *         An persistence manager factory.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instances for parameters in the transient or persistent state,
     *         or the corresponding persistent instance for detached parameter instances,
     *         in the same order as in the parameter array. (= The value returned from
     *         {@link PersistenceManager#makePersistentAll(Object...)}).
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @throws JDOUserException
     *         {@code PersistenceManager.}{@link PersistenceManager#makePersistentAll(Object...)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public TEntity[] putAll(PersistenceManagerFactory factory, TEntity... entities)
    {
        checkNonNull(factory,  "factory");
        checkNonNull(entities, "entities");

        PersistenceManager manager = createManager(factory);

        try
        {
            return putAll(manager, entities);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Make entities persistent.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param entities
     *         Entities to be made persistent.
     *
     * @return
     *         The parameter instances for parameters in the transient or persistent state,
     *         or the corresponding persistent instance for detached parameter instances,
     *         in the same order as in the parameter array. (= The value returned from
     *         {@link PersistenceManager#makePersistentAll(Object...)}).
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code entities} is {@code null}.
     *
     * @throws JDOUserException
     *         {@code manager.}{@link PersistenceManager#makePersistentAll(Object...)
     *         makePersistentAll(entities)} failed.
     *
     * @since 1.6
     */
    public TEntity[] putAll(PersistenceManager manager, TEntity... entities)
    {
        checkNonNull(manager,  "manager");
        checkNonNull(entities, "entities");

        return manager.makePersistentAll(entities);
    }


    /**
     * Get all entities.
     * A persistence manager factory must be set before this method is called.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.11
     */
    public List<TEntity> getAll() throws IllegalStateException
    {
        checkFactory();

        return getAll(factory);
    }


    /**
     * Get all entities.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.11
     */
    public List<TEntity> getAll(PersistenceManagerFactory factory)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getAll(manager);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get all entities.
     *
     * @param manager
     *         A persistence manager.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}.
     *
     * @since 1.11
     */
    @SuppressWarnings("unchecked")
    public List<TEntity> getAll(PersistenceManager manager) throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");

        Query query = manager.newQuery(entityClass);

        return (List<TEntity>)query.execute();
    }


    /**
     * Get an entity using a condition that identifies the unique entity.
     * A persistence manager factory must be set before this method is called.
     *
     * @param field
     *         A database column name which has UNIQUE constraint.
     *
     * @param value
     *         A value of the data field which identifies the unique entity.
     *
     * @return
     *         A unique entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code field} is {@code null} or {@code value} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.5
     */
    public TEntity getUnique(String field, Object value)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(field, "field");
        checkNonNull(value, "value");
        checkFactory();

        return getUnique(factory, field, value);
    }


    /**
     * Get an entity using a condition that identifies the unique entity.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param field
     *         A database column name which has UNIQUE constraint.
     *
     * @param value
     *         A value of the data field which identifies the unique entity.
     *
     * @return
     *         A unique entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, {@code field} is {@code null},
     *         or {@code value} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.6
     */
    public TEntity getUnique(PersistenceManagerFactory factory, String field, Object value)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(field,   "field");
        checkNonNull(value,   "value");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getUnique(manager, field, value);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get an entity using a condition that identifies the unique entity.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param field
     *         A database column name which has UNIQUE constraint.
     *
     * @param value
     *         A value of the data field which identifies the unique entity.
     *
     * @return
     *         A unique entity, or {@code null} if not found.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, {@code field} is {@code null},
     *         or {@code value} is {@code null}.
     *
     * @since 1.6
     */
    @SuppressWarnings("unchecked")
    public TEntity getUnique(PersistenceManager manager, String field, Object value)
            throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");
        checkNonNull(field,   "field");
        checkNonNull(value,   "value");

        Query query = manager.newQuery(entityClass);
        query.setUnique(true);
        query.setFilter(field + " == :value");

        return (TEntity)query.execute(value);
    }


    /**
     * Get entities using a condition that filters them.
     * A persistence manager factory must be set before this method is called.
     *
     * @param field
     *         A database column name.
     *
     * @param value
     *         A value of the data field which filters entities.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalArgumentException
     *         {@code field} is {@code null} or {@code value} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.7
     */
    public List<TEntity> getList(String field, Object value)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(field, "field");
        checkNonNull(value, "value");
        checkFactory();

        return getList(factory, field, value);
    }


    /**
     * Get entities using a condition that filters them.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param field
     *         A database column name.
     *
     * @param value
     *         A value of the data field which filters entities.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, {@code field} is {@code null},
     *         or {@code value} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.7
     */
    public List<TEntity> getList(PersistenceManagerFactory factory, String field, Object value)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(field,   "field");
        checkNonNull(value,   "value");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getList(manager, field, value);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get entities using a condition that filters them.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param field
     *         A database column name.
     *
     * @param value
     *         A value of the data field which filters entities.
     *
     * @return
     *         Entity list.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, {@code field} is {@code null},
     *         or {@code value} is {@code null}.
     *
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public List<TEntity> getList(PersistenceManager manager, String field, Object value)
            throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");
        checkNonNull(field,   "field");
        checkNonNull(value,   "value");

        Query query = manager.newQuery(entityClass);
        query.setFilter(field + " == :value");

        return (List<TEntity>)query.execute(value);
    }


    /**
     * Get entities using JDOQL.
     * A persistence manager factory must be set before this method is called.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         Entities.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.7
     */
    public List<TEntity> getListByQuery(String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(jdoql, "jdoql");
        checkFactory();

        return getListByQuery(factory, jdoql, parameters);
    }


    /**
     * Get entities using JDOQL.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         Entities.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.7
     */
    public List<TEntity> getListByQuery(PersistenceManagerFactory factory, String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(jdoql,   "jdoql");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getListByQuery(manager, jdoql, parameters);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get entities using JDOQL.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         Entities.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @since 1.7
     */
    @SuppressWarnings("unchecked")
    public List<TEntity> getListByQuery(PersistenceManager manager, String jdoql, Object... parameters)
            throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");
        checkNonNull(jdoql,   "jdoql");

        Query query = manager.newQuery(jdoql);
        query.setClass(entityClass);

        return (List<TEntity>)query.executeWithArray(parameters);
    }


    /**
     * Get an entity using JDOQL. If there are multiple entities that
     * match the query, the first one in the query result is returned.
     * A persistence manager factory must be set before this method is called.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         An entity. {@code null} if no entity matches the given query.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         A persistence manager factory is not set. Or,
     *         failed to create a persistence manager from the
     *         persistence manager factory that this instance holds
     *         (= {@code PersistenceManagerFactory.}{@link
     *         PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed).
     *
     * @since 1.8
     */
    public TEntity getByQuery(String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(jdoql, "jdoql");
        checkFactory();

        return getByQuery(factory, jdoql, parameters);
    }


    /**
     * Get an entity using JDOQL. If there are multiple entities that
     * match the query, the first one in the query result is returned.
     *
     * @param factory
     *         A persistence manager factory.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         An entity. {@code null} if no entity matches the given query.
     *
     * @throws IllegalArgumentException
     *         {@code factory} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @throws IllegalStateException
     *         {@code factory.}{@link PersistenceManagerFactory#getPersistenceManager()
     *         getPersistenceManager()} failed.
     *
     * @since 1.8
     */
    public TEntity getByQuery(PersistenceManagerFactory factory, String jdoql, Object... parameters)
            throws IllegalArgumentException, IllegalStateException
    {
        checkNonNull(factory, "factory");
        checkNonNull(jdoql,   "jdoql");

        PersistenceManager manager = createManager(factory);

        try
        {
            return getByQuery(manager, jdoql, parameters);
        }
        finally
        {
            manager.close();
        }
    }


    /**
     * Get an entity using JDOQL. If there are multiple entities that
     * match the query, the first one in the query result is returned.
     *
     * @param manager
     *         A persistence manager.
     *
     * @param jdoql
     *         JDOQL.
     *
     * @param parameters
     *         Parameters of the JDOQL.
     *
     * @return
     *         An entity. {@code null} if no entity matches the given query.
     *
     * @throws IllegalArgumentException
     *         {@code manager} is {@code null}, or {@code jdoql} is {@code null}.
     *
     * @since 1.8
     */
    @SuppressWarnings("unchecked")
    public TEntity getByQuery(PersistenceManager manager, String jdoql, Object... parameters)
            throws IllegalArgumentException
    {
        checkNonNull(manager, "manager");
        checkNonNull(jdoql,   "jdoql");

        Query query = manager.newQuery(jdoql);
        query.setClass(entityClass);

        List<TEntity> list = (List<TEntity>)query.executeWithArray(parameters);

        if (list == null || list.size() == 0)
        {
            return null;
        }

        return list.get(0);
    }
}
