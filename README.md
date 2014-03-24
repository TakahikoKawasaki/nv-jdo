nv-jdo
======

Overview
--------

Utilities related to JDO.

* Dao - Generic DAO implementation based on JDO.
* Task - Task executed by TaskExecutor.
* TaskExecutor - Task executor.


License
-------

Apache License, Version 2.0


Download
--------

    git clone https://github.com/TakahikoKawasaki/nv-jdo.git


Javadoc
-------

[nv-jdo JavaDoc](http://TakahikoKawasaki.github.com/nv-jdo/)


Example
-------

```java
//------------------------------------------------------
// Example of Dao, which is a generic DAO implementation
// based on JDO.
//------------------------------------------------------

// Persistence manager factory.
 PersistenceManagerFactory factory = ...;

// Create a DAO with an entity class and the persistence manager factory.
Dao<Customer> dao = new Dao<Customer>(Customer.class, factory);

// Or using a static method.
Dao<Customer> dao = Dao.create(Customer.class, factory);

// Use the DAO.

// (1) Create an entity.
Customer customer = ...;
dao.put(customer);

// (2) Get an entity.
String customerId = ...;
Customer customer = dao.getById(customerId);

// (3) Delete an entity.
dao.delete(customer);

// (4) Delete an entity by ID.
dao.deleteById(customerId);
```

```java
//------------------------------------------------------
// Example of TaskExecutor.
//------------------------------------------------------
// Persistence manager factory.
PersistenceManagerFactory factory = ...;

// Create a task executor.
TaskExecutor executor = new TaskExecutor(factory);

// Create a task.
Task task = new Task() {
    @Override
    public Object run(PersistenceManager manager) {
        ......
    }
};

// Execute the task.
executor.execute(task, true, 2);
```

Maven
-----

```xml
<dependency>
    <groupId>com.neovisionaries</groupId>
    <artifactId>nv-jdo</artifactId>
    <version>1.7</version>
</dependency>
```


See Also
--------

* [JDO API 3.0](http://db.apache.org/jdo/api30/apidocs/index.html)


Author
------

Takahiko Kawasaki, Neo Visionaries Inc.
