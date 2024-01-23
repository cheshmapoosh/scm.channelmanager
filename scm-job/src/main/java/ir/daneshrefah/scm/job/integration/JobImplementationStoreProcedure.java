package ir.daneshrefah.scm.job.integration;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@RequiredArgsConstructor
public class JobImplementationStoreProcedure implements Job {

    private final String storeProcedure;
    private final JsonNode payload;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }

}
