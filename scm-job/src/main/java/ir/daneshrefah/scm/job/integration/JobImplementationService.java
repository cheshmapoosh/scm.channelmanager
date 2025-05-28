package ir.daneshrefah.scm.job.integration;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.service.ScmService;
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
public class JobImplementationService implements Job {

    private final ScmService service;
    private final JsonNode payload;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }

}
