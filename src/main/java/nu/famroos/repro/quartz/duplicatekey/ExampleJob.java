package nu.famroos.repro.quartz.duplicatekey;

import java.util.UUID;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExampleJob implements org.quartz.Job
{
	public static final String USER_ID_KEY = "userId";

	private static final Logger logger = LoggerFactory.getLogger(ExampleJob.class);

	@Override
	public void execute(JobExecutionContext context)
	{
		JobDataMap jobData = context.getMergedJobDataMap();
		UUID userId = UUID.fromString((String) jobData.get(USER_ID_KEY));
		logger.info("Executing run for user with ID {}", userId);
	}
}
