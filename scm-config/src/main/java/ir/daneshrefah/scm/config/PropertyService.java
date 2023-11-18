package ir.daneshrefah.scm.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@Service
public class PropertyService {

    private JdbcTemplate jdbcTemplate;

    public PropertyService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Property> find(String application, String profile, String label) {
        String sql = "SELECT * FROM REF.TBL_SFG_PROPERTIES ";

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Property property = new Property(resultSet.getString("application_key"),
                    resultSet.getString("profile_key"),
                    resultSet.getString("label_key"),
                    resultSet.getString("prop_key"),
                    resultSet.getString("prop_key"));
            return property;
        });
    }
}
