package ir.daneshrefah.scm.core.services.constant;

import ir.daneshrefah.scm.common.service.ResourceService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-08
 */
@RequiredArgsConstructor
@Service
public class ResourceServiceImpl implements ResourceService {

    private final Environment env;

    @Override
    public String prepareProperties(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        List<String> parameters = StringUtils.extractPropertyNames(value);
        for (Iterator<String> iterator = parameters.iterator(); iterator.hasNext(); ) {
            String parameter = iterator.next();
            String parameterValue = env.getProperty(parameter);
            value = StringUtils.replace(value, StringUtils.surroundWithCurlyBracesAndDollar(parameter), parameterValue);
        }
        return value;
    }
}
