nv-jdo
======

Overview
--------

Utilities related to JDO.

* JdoDao - Generic DAO implementation based on JDO.


License
-------

Apache License, Version 2.0


Download
--------

    git clone https://github.com/TakahikoKawasaki/nv-jdo.git


Javadoc
-------

[nv-jdo javadoc](http://TakahikoKawasaki.github.com/nv-jdo/)


Example
-------

```java
//--------------------------------------------------
// Example of JdoDao, which is a generic DAO
// implementation based on JDO.
//--------------------------------------------------

// Set up a shared persistence manager factory.
//
// The persistence manager factory set up here is used
// as fail-safe for JdoDao instances which do not have
// their own persistence manager factory.
JdoDao.setupSharedPersistenceManagerFactory("transactions-optional");

// Create a DAO with an entity class.
//
// The example here creates a DAO dedicated to Customer class.
JdoDao<Customer> dao = new JdoDao<Customer>(Customer.class);

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


Maven
-----

```xml
<dependency>
    <groupId>com.neovisionaries</groupId>
    <artifactId>nv-jdo</artifactId>
    <version>1.0</version>
</dependency>
```


See Also
--------

* [JDO API 3.0](http://db.apache.org/jdo/api30/apidocs/index.html)


Author
------

Takahiko Kawasaki, Neo Visionaries Inc.
