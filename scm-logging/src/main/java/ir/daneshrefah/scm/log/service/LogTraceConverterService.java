package ir.daneshrefah.scm.log.service;
import com.vdurmont.semver4j.Requirement;

import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.log.configuration.LogConditions;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.service.LogService;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Conditional(LogConditions.LogTraceCondition.class)
@Slf4j
public class LogTraceConverterService implements ConverterService {

    private final LogService logService;
    private final SpanLogConverterService spanLogConverterService;
    private final Requirement versionRequirement;

    public LogTraceConverterService(LogService logService,
                                    SpanLogConverterService spanLogConverterService,
                                    @Value("${scm.log.logTraceConverter.versionRequirement:#{null}}") String versionRequirement) {
        this.logService = logService;
        this.spanLogConverterService = spanLogConverterService;
        if (versionRequirement != null && !versionRequirement.isEmpty()) {
            this.versionRequirement = Requirement.buildNPM(versionRequirement);
        } else {
            this.versionRequirement = Requirement.buildNPM("*");
        }
    }


    @Override
    public boolean supports(LogMessage logMessage) {
//        try {
//            String version = logMessage.getPayload().getAttributes().get(LogAttribute.VERSION.getAttributeName());
//            return   StringUtils.isEmpty(logMessage.getPayload().getAttributes().get("scm-source"))  && versionRequirement.isSatisfiedBy(version);
//        }catch (Exception ignored) {
//            //TODO
//            return true;
//        }
        return false;
    }

    @PostConstruct
    public void init() {
        log.info(">>> LogTraceConverterService successfully initialized");
    }

    @Override
    public void convertAndPersist(LogMessage logMessage) throws Exception {
        List<LogTraceEntity> entities = convert(logMessage);
        logService.saveAll(entities);
    }

    private List<LogTraceEntity> convert(LogMessage logMessage) throws Exception {
        return spanLogConverterService.mapToLogTraceEntity(logMessage);
    }
}
