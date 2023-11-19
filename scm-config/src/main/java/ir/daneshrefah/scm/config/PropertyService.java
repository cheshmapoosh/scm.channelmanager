package ir.daneshrefah.scm.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

//    @Cacheable(value = "cache_config", key = "#root.methodName + '-' + #application + '-' + #profile + '-' + #label")
    public List<Property> find(String application, String profile, String label) {
        StringBuilder sql = new StringBuilder("SELECT * FROM REF.TBL_SFG_PROPERTIES WHERE APPLICATION_KEY = ? " +
                "AND PROFILE_KEY = ? ");
        List<Object> args = new ArrayList<>();
        args.add(application);
        args.add(profile);
        if (null == label) {
            sql.append("AND LABEL_KEY IS NULL");
        } else {
            sql.append("AND LABEL_KEY = ?");
            args.add(label);
        }

        return jdbcTemplate.query(sql.toString(), (resultSet, rowNum) -> {
            Property property = new Property(resultSet.getString("application_key"),
                    resultSet.getString("profile_key"),
                    resultSet.getString("label_key"),
                    resultSet.getString("prop_key"),
                    resultSet.getString("prop_value"));
            return property;
        }, args.toArray());
    }
}
