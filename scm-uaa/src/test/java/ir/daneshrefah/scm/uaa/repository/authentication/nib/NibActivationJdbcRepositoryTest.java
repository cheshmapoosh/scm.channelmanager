package ir.daneshrefah.scm.uaa.repository.authentication.nib;

import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationDataAccessException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NibActivationJdbcRepositoryTest {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate =
            mock(NamedParameterJdbcTemplate.class);
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final NibActivationJdbcRepository repository =
            new NibActivationJdbcRepository(namedParameterJdbcTemplate, jdbcTemplate);

    @Test
    void translatesDatabaseFailureInsteadOfReturningBusinessAbsence() {
        when(namedParameterJdbcTemplate.query(
                anyString(),
                org.mockito.ArgumentMatchers.<Map<String, ?>>any(),
                org.mockito.ArgumentMatchers.<RowMapper<Integer>>any()
        )).thenThrow(new DataAccessResourceFailureException("database unavailable"));

        assertThrows(NibActivationDataAccessException.class, repository::findParentNibChannelId);
    }
}
