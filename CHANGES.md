CHANGES
=======

1.19 (2022-05-30)
-----------------

- `TaskExecutor` class
    * Added `TaskExecutor(PersistenceManager)` constructor.
    * Added `execute(Task, int)` method.
    * Added `getFactory()` method.
    * Added `getManager()` method.


1.18 (2018-07-09)
-----------------

- Added `executeListWithArrayByQuery` methods to `Dao` class.
- Added `executeListWithMapByQuery` methods to `Dao` class.


1.17 (2017-08-03)
-----------------

- Added `QueryLanguage` enum.
- Added `executeWithArrayByQuery` methods to `Dao` class.
- Added `executeWithMapByQuery` methods to `Dao` class.


1.16 (2017-04-15)
-----------------

- Added `TransactionAwareTask` interface.
- Added `TaskAdapter` class.
- Updated `TaskExecutor` to handle `TransactionAwareTask`.
