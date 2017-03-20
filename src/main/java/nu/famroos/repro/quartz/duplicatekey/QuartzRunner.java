/*******************************************************************************
 * Copyright (c) 2016 Stichting Yona Foundation This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *******************************************************************************/
package nu.famroos.repro.quartz.duplicatekey;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class QuartzRunner implements CommandLineRunner
{
	private static final String JOB_NAME = "ExampleJob";
	private static final String GROUP_NAME = "ExampleGroup";
	private static final String TRIGGER_NAME = "ExampleTrigger";

	private static final Logger logger = LoggerFactory.getLogger(QuartzRunner.class);

	@Autowired
	private Scheduler scheduler;

	@Override
	public void run(String... args) throws Exception
	{
		if (args.length > 0)
		{
			scheduler.addJob(JobBuilder.newJob(ExampleJob.class).storeDurably().requestRecovery()
					.withDescription("Some nice description").withIdentity(JOB_NAME, GROUP_NAME).build(), false);
			return;
		}
		scheduler.start();

		schedule(UUID.randomUUID(), Date.from(ZonedDateTime.now().plusSeconds(5).toInstant()));
		schedule(UUID.randomUUID(), Date.from(ZonedDateTime.now().plusSeconds(10).toInstant()));
	}

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
}
