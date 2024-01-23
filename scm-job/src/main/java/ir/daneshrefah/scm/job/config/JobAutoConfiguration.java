package ir.daneshrefah.scm.job.config;

import ir.daneshrefah.scm.job.integration.JobImplementationService;
import ir.daneshrefah.scm.job.model.JobDefinition;
import ir.daneshrefah.scm.job.service.JobService;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Configuration
public class JobAutoConfiguration {

    public void initializeJobs(JobService service) {
        List<JobDefinition> jobDefinitions = service.findAllJobDefinition();
        for (Iterator<JobDefinition> iterator = jobDefinitions.iterator(); iterator.hasNext(); ) {
            JobDefinition jobDefinition = iterator.next();
            createJobInstance(jobDefinition);
        }
    }

    private void createJobInstance(JobDefinition jobDefinition) {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobDefinition.getName() + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(jobDefinition.getCronExpression()))
                .build();

        /*JobDetail jobDetail = JobBuilder.newJob(JobImplementationService.class)
                .withIdentity(jobDefinition.getName())
                .usingJobData("service", jobDefinition.getService().getCode())
                .usingJobData("payload", jobDefinition.getPayload().toString())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);*/
    }

}
