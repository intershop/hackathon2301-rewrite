FAQ for Work with ICM Responsive
================================

[TOC]: #

# Table of Contents
- [Question: Access rights build folder](#question-access-rights-build-folder)

## Question Access rights build folder 

If you run dbPrepare or startServer the docker service creates a build folder. This one is created for example like this 

```
drwxr--r-- 13 root   root    4096 Jan 20 10:34 build
```

now you get an error like this in the output

```
{"@timestamp":"2022-01-20T09:40:24.569+00:00","@version":"1","message":"Failure: [migrate]pf_site_prepare:pre.Class0 SiteContentPreparer [mode=replace] 25ms, see the error log. Error message: Failed with error preparing sites","logger_name":"com.intershop.tool.dbprepare.execute.ExecutionLoggerImpl","thread_name":"main","level":"ERROR","level_value":40000,"stack_trace":"java.lang.RuntimeException: Failed with error preparing sites\n\tat com.intershop.site.dbinit.SiteContentPreparer.executeTasks(SiteContentPreparer.java:190)\n\tat com.intershop.site.dbinit.SiteContentPreparer.doPreparation(SiteContentPreparer.java:133)\n\tat com.intershop.site.dbmigrate.SiteContentPreparer.migrate(SiteContentPreparer.java:21)\n\tat com.intershop.tool.dbprepare.execute.ExecutorFactoryImpl.lambda$new$1(ExecutorFactoryImpl.java:35)\n\tat com.intershop.tool.dbprepare.execute.DelegatingExecutor.executeDelegate(DelegatingExecutor.java:56)\n\tat com.intershop.tool.dbprepare.execute.TransactionalExecutor.lambda$execute$0(TransactionalExecutor.java:33)\n\tat com.intershop.beehive.orm.capi.transaction.Transaction.execute(Transaction.java:153)\n\tat com.intershop.tool.dbprepare.execute.TransactionalExecutor.execute(TransactionalExecutor.java:33)\n\tat com.intershop.tool.dbprepare.execute.DelegatingExecutor.executeDelegate(DelegatingExecutor.java:56)\n\tat com.intershop.tool.dbprepare.execute.ValidatingExecutor.execute(ValidatingExecutor.java:28)\n\tat com.intershop.tool.dbprepare.execute.DelegatingExecutor.executeDelegate(DelegatingExecutor.java:56)\n\tat com.intershop.tool.dbprepare.execute.LoggingExecutor.execute(LoggingExecutor.java:45)\n\tat com.intershop.tool.dbprepare.execute.DelegatingExecutor.executeDelegate(DelegatingExecutor.java:56)\n\tat com.intershop.tool.dbprepare.execute.CartridgeContextExecutor.execute(CartridgeContextExecutor.java:48)\n\tat com.intershop.tool.dbprepare.execute.DelegatingExecutor.executeDelegate(DelegatingExecutor.java:56)\n\tat com.intershop.tool.dbprepare.execute.ExceptionHandlingExecutor.execute(ExceptionHandlingExecutor.java:39)\n\tat com.intershop.tool.dbprepare.execute.DBPrepareExecutor.execute(DBPrepareExecutor.java:97)\n\tat java.base/java.util.ArrayList.forEach(Unknown Source)\n\tat com.intershop.tool.dbprepare.execute.DBPrepareExecutor.run(DBPrepareExecutor.java:71)\n\tat com.intershop.tool.dbprepare.DBPrepare.lambda$run$2(DBPrepare.java:52)\n\tat java.base/java.util.ArrayList.forEach(Unknown Source)\n\tat com.intershop.tool.dbprepare.DBPrepare.run(DBPrepare.java:52)\n\tat com.intershop.tool.dbprepare.DBPrepare.main(DBPrepare.java:35)\n\tSuppressed: java.nio.file.NoSuchFileException: /intershop/sites/root/units/root/impex/config/DBInit-ac_order_status_import_xml_services.properties\n\t\tat java.base/sun.nio.fs.UnixException.translateToIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(Unknown Source)\n\t\tat java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.copy(Unknown Source)\n\t\tat com.intershop.site.internal.content.FileContentHashingCopier.copy(FileContentHashingCopier.java:40)\n\t\tat com.intershop.site.internal.content.BaseSiteContenHandler.copy(BaseSiteContenHandler.java:82)\n\t\tat com.intershop.site.internal.content.SiteContentDiffCheckingReplacingCopyHandler.lambda$createCopyTask$0(SiteContentDiffCheckingReplacingCopyHandler.java:26)\n\t\tat com.intershop.site.dbinit.SiteContentPreparer.lambda$toSafeCallable$2(SiteContentPreparer.java:215)\n\t\tat com.intershop.beehive.core.internal.execute.ExecutorServiceImpl$ClosingCurrentResourcesCallable.call(ExecutorServiceImpl.java:150)\n\t\tat java.base/java.util.concurrent.FutureTask.run(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)\n\t\tat java.base/java.lang.Thread.run(Unknown Source)\n\tSuppressed: java.nio.file.NoSuchFileException: /intershop/sites/root/units/root/impex/src/Users.xml\n\t\tat java.base/sun.nio.fs.UnixException.translateToIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(Unknown Source)\n\t\tat java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.copy(Unknown Source)\n\t\tat com.intershop.site.internal.content.FileContentHashingCopier.copy(FileContentHashingCopier.java:40)\n\t\tat com.intershop.site.internal.content.BaseSiteContenHandler.copy(BaseSiteContenHandler.java:82)\n\t\tat com.intershop.site.internal.content.SiteContentDiffCheckingReplacingCopyHandler.lambda$createCopyTask$0(SiteContentDiffCheckingReplacingCopyHandler.java:26)\n\t\tat com.intershop.site.dbinit.SiteContentPreparer.lambda$toSafeCallable$2(SiteContentPreparer.java:215)\n\t\tat com.intershop.beehive.core.internal.execute.ExecutorServiceImpl$ClosingCurrentResourcesCallable.call(ExecutorServiceImpl.java:150)\n\t\tat java.base/java.util.concurrent.FutureTask.run(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)\n\t\tat java.base/java.lang.Thread.run(Unknown Source)\n\tSuppressed: java.nio.file.NoSuchFileException: /intershop/sites/root/units/root/impex/config/DBInit-UserImport.properties\n\t\tat java.base/sun.nio.fs.UnixException.translateToIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(Unknown Source)\n\t\tat java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.copy(Unknown Source)\n\t\tat com.intershop.site.internal.content.FileContentHashingCopier.copy(FileContentHashingCopier.java:40)\n\t\tat com.intershop.site.internal.content.BaseSiteContenHandler.copy(BaseSiteContenHandler.java:82)\n\t\tat com.intershop.site.internal.content.SiteContentDiffCheckingReplacingCopyHandler.lambda$createCopyTask$0(SiteContentDiffCheckingReplacingCopyHandler.java:26)\n\t\tat com.intershop.site.dbinit.SiteContentPreparer.lambda$toSafeCallable$2(SiteContentPreparer.java:215)\n\t\tat com.intershop.beehive.core.internal.execute.ExecutorServiceImpl$ClosingCurrentResourcesCallable.call(ExecutorServiceImpl.java:150)\n\t\tat java.base/java.util.concurrent.FutureTask.run(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)\n\t\tat java.base/java.lang.Thread.run(Unknown Source)\n\tSuppressed: java.nio.file.NoSuchFileException: /intershop/sites/root/.active\n\t\tat java.base/sun.nio.fs.UnixException.translateToIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixException.rethrowAsIOException(Unknown Source)\n\t\tat java.base/sun.nio.fs.UnixFileSystemProvider.newByteChannel(Unknown Source)\n\t\tat java.base/java.nio.file.spi.FileSystemProvider.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.newOutputStream(Unknown Source)\n\t\tat java.base/java.nio.file.Files.copy(Unknown Source)\n\t\tat com.intershop.site.internal.content.FileContentHashingCopier.copy(FileContentHashingCopier.java:40)\n\t\tat com.intershop.site.internal.content.BaseSiteContenHandler.copy(BaseSiteContenHandler.java:82)\n\t\tat com.intershop.site.internal.content.SiteContentDiffCheckingReplacingCopyHandler.lambda$createCopyTask$0(SiteContentDiffCheckingReplacingCopyHandler.java:26)\n\t\tat com.intershop.site.dbinit.SiteContentPreparer.lambda$toSafeCallable$2(SiteContentPreparer.java:215)\n\t\tat com.intershop.beehive.core.internal.execute.ExecutorServiceImpl$ClosingCurrentResourcesCallable.call(ExecutorServiceImpl.java:150)\n\t\tat java.base/java.util.concurrent.FutureTask.run(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(Unknown Source)\n\t\tat java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(Unknown Source)\n\t\tat java.base/java.lang.Thread.run(Unknown Source)\n","requestapplication":"[migrate]pf_site_prepare:pre.Class0 SiteContentPreparer [mode=replace]","tags":["DBPrepare"]}
```

in the docker container the applications are running as "intershop" user. This user has no write access for the shared build folder.

## Answer Access rights build folder

Get the other users write access to the shared build folder.

```
chmod -R o+wx build
```
