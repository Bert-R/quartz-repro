Small project to show how one job can have multiple having multiple triggers that fire at different moments.

Originally, this repo was created to support [this StackOverflow question](http://stackoverflow.com/questions/42911846/exception-when-creating-second-trigger-for-same-quartz-job). That issue was resolved by correcting [the Liquibase change log](src/main/resources/db/changelog/db.changelog-master.yaml). After that, the code here is still useful, as an example on using [Quartz scheduler](http://www.quartz-scheduler.org/).

To initialize the database, run ``gradlew -PappArgs="['init']" bootRun``. This will load the Quartz database schema in the database ``jdbc:mariadb://localhost:3306/qrtz``, with the user name ``qrtz`` and the password ``qrtz``. Edit ``application.properties`` if you want to use something different. Next, it creates a Quartz job named ``ExampleJob`` in the group ``ExampleGroup``.

To run the job twice, with delays of 5 and 10 seconds, run ``gradlew bootRun``. This is the way the jobs are scheduled:

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

Hope it's somehow useful.
