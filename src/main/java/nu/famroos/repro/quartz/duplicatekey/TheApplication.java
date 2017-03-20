package nu.famroos.repro.quartz.duplicatekey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("nu.famroos.repro.quartz.duplicatekey")
@SpringBootApplication
public class TheApplication extends CachingConfigurerSupport
{
	public static void main(String[] args)
	{
		SpringApplication app = new SpringApplication(TheApplication.class);
		app.setWebEnvironment(false);
		app.run(args);
	}
}
