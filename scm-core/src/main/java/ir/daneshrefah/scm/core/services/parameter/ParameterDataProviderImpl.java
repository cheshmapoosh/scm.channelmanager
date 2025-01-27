package ir.daneshrefah.scm.core.services.parameter;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.plugin.api.service.ParameterDataProvider;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
@Component
@RequiredArgsConstructor
public class ParameterDataProviderImpl extends ParameterDataProvider {

    private final CacheTemplate cacheTemplate;
    private final PersonService personService;
    private final Environment environment;


    @PostConstruct
    public void init() {
        setInstance(this);
    }

    @Override
    public Optional<Object> extractParameterValue(Parameter parameter) {
        return extractParameterValue(null, parameter);
    }

    @Override
    public Optional<Object> extractParameterValue(Message message, Parameter parameter) {
        if (isValidParameter(parameter)) {
            Object value = switch (parameter.getDatasource().getProperty()) {
                case MESSAGE_VARIABLE -> provideMessageVariable(parameter, message);
                case STATIC -> provideStaticVariable(parameter);
                case DATE_YYYYMMDD -> provideDateVariable();
                case DATE_SHAMSI_YYYYMMDD -> provideShamsiDateVariable();
                case DATE_SHAMSI_YYYYMMDDHHMMDD -> provideShamsiDateTimeVariable();
                case CACHE_VARIABLE -> provideCacheVariable(parameter);
                case TERMINAL_CODE -> provideTerminalCodeVariable(); //provide from authentication
                case HTTP_STATUS_CODE -> provideHttpStatusCodeVariable(message);
                case CORRELATION_ID -> provideCorrelationId();
                case CONFIG_VARIABLE -> provideValueFromConfig(parameter);
                case RESOURCE_VARIABLE -> null;
                case PROVIDER_TERMINAL_CODE -> null;
                case AUTHENTICATION_USERNAME -> null;
                case AUTHENTICATION_NICKNAME -> null;
                case AUTHENTICATION_EFFECTIVE_USERNAME -> null;
                case AUTHENTICATION_EFFECTIVE_NICKNAME -> null;
                case AUTHENTICATION_DELEGATOR_USERNAME -> null;
                case AUTHENTICATION_DELEGATOR_NICKNAME -> null;
                case AUTHENTICATION_NATIONAL_ID -> provideAuthenticationNationalId(parameter);
                case AUTHENTICATION_SUB_ORGANIZATION -> provideAuthenticationSubOrg(parameter);
            };
            return Optional.ofNullable(value);
        }
        return Optional.empty();
    }

    private Object provideAuthenticationSubOrg(Parameter parameter) {
        Authentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert authentication != null;
        UserProfile profile = authentication.getProfile();
        Long personId = profile.getPersonId();
        GeneralPerson foundPerson = personService.findPersonByPersonId(personId);
        String subOrganizationId = null;
        if (foundPerson instanceof GeneralLegalPerson legalPerson) {
            subOrganizationId = legalPerson.getSubOrganizationId();
        }
        return Objects.nonNull(subOrganizationId) && !subOrganizationId.isBlank()
                ? subOrganizationId
                : parameter.getDefaultValue();
    }

    private Object provideShamsiDateTimeVariable() {
        return DateUtils.ShamsiCalendarConvertor.convertToShamsiDateString(LocalDateTime.now(), "yyyyMMddHHmmss");
    }

    private Object provideValueFromConfig(Parameter parameter) {
        String value = parameter.getDatasource().getValue();
        String propertyKey = StringUtils.replacePrefixAndSuffix(value,"${","","}","");
        return environment.getProperty(propertyKey, parameter.getDefaultValue());
    }

    private Object provideAuthenticationNationalId(Parameter parameter) {
        Authentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert authentication != null;
        UserProfile profile = authentication.getProfile();
        Long personId = profile.getPersonId();
        GeneralPerson foundPerson = personService.findPersonByPersonId(personId);
        String nationalId = null;
        if (foundPerson instanceof GeneralRealPerson realPerson) {
            nationalId = realPerson.getNationalCode();
        } else if (foundPerson instanceof GeneralLegalPerson legalPerson) {
            nationalId = legalPerson.getNationalId();
        }
        return Objects.nonNull(nationalId) && !nationalId.isBlank()
                ? nationalId
                : parameter.getDefaultValue();
    }


    private Object provideCorrelationId() {
        return MessageInputContext.getCurrentContext().getCorrelationId();
    }

    private Object provideHttpStatusCodeVariable(Message message) {
        return message.getHeader().getHttpHeader().getHttpStatusCode();
    }

    private Object provideTerminalCodeVariable() {
        Authentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.nonNull(authentication)) {
            return authentication.getTerminalCode();
        }
        return null;
    }

    private Object provideCacheVariable(Parameter parameter) {
        ParameterDatasource datasource = parameter.getDatasource();
        // mapName.key
        String cacheKey = datasource.getValue();
        String[] split = cacheKey.split("\\.");
        return cacheTemplate.getFromCache(split[0], split[1]);
    }

    private Object provideShamsiDateVariable() {
        return DateUtils.ShamsiCalendarConvertor.convertToShamsiDateString(new Date(), "yyyyMMdd");
    }

    private Object provideDateVariable() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(new Date());
    }

    private Object provideStaticVariable(Parameter parameter) {
        return parameter.getDatasource().getValue();
    }

    private Object provideMessageVariable(Parameter parameter, Message message) {
        String dataSource = parameter.getDatasource().getValue();
        if (StringUtils.isBlank(dataSource)) {
            return null;
        }
        return message.getPayload().get(dataSource);
    }

    private boolean isValidParameter(Parameter parameter) {
        return !Objects.isNull(parameter) &&
               !Objects.isNull(parameter.getDatasource()) &&
               !Objects.isNull(parameter.getDatasource().getProperty());
    }


}
