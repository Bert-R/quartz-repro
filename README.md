Small project to reproduce an issue when having multiple triggers for the same Quartz job.

To initialize the database, run ``gradlew -PappArgs="['init']" bootRun``. This will load the Quartz database schema in the database ``jdbc:mariadb://localhost:3306/qrtz``, with the username ``qrtz`` and the password ``qrtz``. Edit ``application.properties`` if you want to use something different. Next, it creates a Quartz job named ``ExampleJob`` in the group ``ExampleGroup``.

To reproduce the issue, run ``gradlew bootRun``. That will schedule the job twice:

```
		schedule(UUID.randomUUID(), Date.from(ZonedDateTime.now().plusSeconds(5).toInstant()));
		schedule(UUID.randomUUID(), Date.from(ZonedDateTime.now().plusSeconds(10).toInstant()));
```

The ``schedule`` method is implemented as follows:

```
	private void schedule(UUID userId, Date date) throws SchedulerException
	{
		logger.info("Scheduling job {} in group {} at {}", JOB_NAME, GROUP_NAME, date);
		
		JobDetail job = scheduler.getJobDetail(JobKey.jobKey(JOB_NAME, GROUP_NAME));
		TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_NAME + "_" + userId, GROUP_NAME.toString());
		Trigger trigger = newTrigger()
				.forJob(job)
				.withIdentity(triggerKey)
				.usingJobData(new JobDataMap(Collections.singletonMap(ExampleJob.USER_ID_KEY, userId.toString())))
				.startAt(date)
				.withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
				.build();
		scheduler.scheduleJob(trigger);
	}
```

Running this results in the following exception:

```
java.lang.IllegalStateException: Failed to execute CommandLineRunner
        at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:803) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:784) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.afterRefresh(SpringApplication.java:771) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at nu.famroos.repro.quartz.duplicatekey.TheApplication.main(TheApplication.java:16) [main/:na]
Caused by: org.quartz.JobPersistenceException: Couldn't store trigger 'ExampleGroup.ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206' for 'ExampleGroup.ExampleJob' job:(conn:974) Duplicate entry 'schedulerFactoryBean' for key 'PRIMARY'
Query is: INSERT INTO QRTZ_SIMPLE_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, REPEAT_COUNT, REPEAT_INTERVAL, TIMES_TRIGGERED)  VALUES('schedulerFactoryBean', ?, ?, ?, ?, ?), parameters ['ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206','ExampleGroup',0,0,0]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.storeTrigger(JobStoreSupport.java:1223) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$4.executeVoid(JobStoreSupport.java:1159) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$VoidTransactionCallback.execute(JobStoreSupport.java:3703) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$VoidTransactionCallback.execute(JobStoreSupport.java:3701) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreCMT.executeInLock(JobStoreCMT.java:245) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.storeTrigger(JobStoreSupport.java:1155) ~[quartz-2.2.1.jar:na]
        at org.quartz.core.QuartzScheduler.scheduleJob(QuartzScheduler.java:932) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.StdScheduler.scheduleJob(StdScheduler.java:258) ~[quartz-2.2.1.jar:na]
        at nu.famroos.repro.quartz.duplicatekey.QuartzRunner.schedule(QuartzRunner.java:69) ~[main/:na]
        at nu.famroos.repro.quartz.duplicatekey.QuartzRunner.run(QuartzRunner.java:53) ~[main/:na]
        at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:800) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        ... 4 common frames omitted
Caused by: java.sql.SQLIntegrityConstraintViolationException: (conn:974) Duplicate entry 'schedulerFactoryBean' for key 'PRIMARY'
Query is: INSERT INTO QRTZ_SIMPLE_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, REPEAT_COUNT, REPEAT_INTERVAL, TIMES_TRIGGERED)  VALUES('schedulerFactoryBean', ?, ?, ?, ?, ?), parameters ['ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206','ExampleGroup',0,0,0]
        at org.mariadb.jdbc.internal.util.ExceptionMapper.get(ExceptionMapper.java:128) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.internal.util.ExceptionMapper.getException(ExceptionMapper.java:101) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.internal.util.ExceptionMapper.throwAndLogException(ExceptionMapper.java:77) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.MariaDbStatement.executeQueryEpilog(MariaDbStatement.java:224) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.MariaDbServerPreparedStatement.executeInternal(MariaDbServerPreparedStatement.java:411) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.MariaDbServerPreparedStatement.execute(MariaDbServerPreparedStatement.java:359) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.MariaDbServerPreparedStatement.executeUpdate(MariaDbServerPreparedStatement.java:348) ~[mariadb-java-client-1.5.5.jar:na]
        at org.quartz.impl.jdbcjobstore.SimpleTriggerPersistenceDelegate.insertExtendedTriggerProperties(SimpleTriggerPersistenceDelegate.java:63) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.StdJDBCDelegate.insertTrigger(StdJDBCDelegate.java:1098) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.storeTrigger(JobStoreSupport.java:1220) ~[quartz-2.2.1.jar:na]
        ... 14 common frames omitted
Caused by: org.mariadb.jdbc.internal.util.dao.QueryException: Duplicate entry 'schedulerFactoryBean' for key 'PRIMARY'
Query is: INSERT INTO QRTZ_SIMPLE_TRIGGERS (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, REPEAT_COUNT, REPEAT_INTERVAL, TIMES_TRIGGERED)  VALUES('schedulerFactoryBean', ?, ?, ?, ?, ?), parameters ['ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206','ExampleGroup',0,0,0]
        at org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol.getResult(AbstractQueryProtocol.java:1114) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol.executePreparedQuery(AbstractQueryProtocol.java:602) ~[mariadb-java-client-1.5.5.jar:na]
        at org.mariadb.jdbc.MariaDbServerPreparedStatement.executeInternal(MariaDbServerPreparedStatement.java:398) ~[mariadb-java-client-1.5.5.jar:na]
        ... 19 common frames omitted
```

The Quartz data is now inconsistent to a level that the scheduler even doesn't start anymore:

```
java.lang.IllegalStateException: Failed to execute CommandLineRunner
        at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:803) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.callRunners(SpringApplication.java:784) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.afterRefresh(SpringApplication.java:771) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at org.springframework.boot.SpringApplication.run(SpringApplication.java:316) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        at nu.famroos.repro.quartz.duplicatekey.TheApplication.main(TheApplication.java:16) [main/:na]
Caused by: org.quartz.SchedulerConfigException: Failure occured during job recovery.
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.schedulerStarted(JobStoreSupport.java:692) ~[quartz-2.2.1.jar:na]
        at org.quartz.core.QuartzScheduler.start(QuartzScheduler.java:567) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.StdScheduler.start(StdScheduler.java:142) ~[quartz-2.2.1.jar:na]
        at nu.famroos.repro.quartz.duplicatekey.QuartzRunner.run(QuartzRunner.java:50) ~[main/:na]
        at org.springframework.boot.SpringApplication.callRunner(SpringApplication.java:800) ~[spring-boot-1.4.2.RELEASE.jar:1.4.2.RELEASE]
        ... 4 common frames omitted
Caused by: org.quartz.JobPersistenceException: Couldn't retrieve trigger: No record found for selection of Trigger with key: 'ExampleGroup.ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206' and statement: SELECT * FROM QRTZ_SIMPLE_TRIGGERS WHERE SCHED_NAME = 'schedulerFactoryBean' AND TRIGGER_NAME = ? AND TRIGGER_GROUP = ?
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.retrieveTrigger(JobStoreSupport.java:1533) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.recoverMisfiredJobs(JobStoreSupport.java:979) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.recoverJobs(JobStoreSupport.java:866) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$1.executeVoid(JobStoreSupport.java:838) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$VoidTransactionCallback.execute(JobStoreSupport.java:3703) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport$VoidTransactionCallback.execute(JobStoreSupport.java:3701) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.executeInNonManagedTXLock(JobStoreSupport.java:3787) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.recoverJobs(JobStoreSupport.java:834) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.schedulerStarted(JobStoreSupport.java:690) ~[quartz-2.2.1.jar:na]
        ... 8 common frames omitted
Caused by: java.lang.IllegalStateException: No record found for selection of Trigger with key: 'ExampleGroup.ExampleTrigger_73e40a3f-f5fd-4c1b-81a3-5ac09e014206' and statement: SELECT * FROM QRTZ_SIMPLE_TRIGGERS WHERE SCHED_NAME = 'schedulerFactoryBean' AND TRIGGER_NAME = ? AND TRIGGER_GROUP = ?
        at org.quartz.impl.jdbcjobstore.SimpleTriggerPersistenceDelegate.loadExtendedTriggerProperties(SimpleTriggerPersistenceDelegate.java:95) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.StdJDBCDelegate.selectTrigger(StdJDBCDelegate.java:1819) ~[quartz-2.2.1.jar:na]
        at org.quartz.impl.jdbcjobstore.JobStoreSupport.retrieveTrigger(JobStoreSupport.java:1531) ~[quartz-2.2.1.jar:na]
        ... 16 common frames omitted
```
