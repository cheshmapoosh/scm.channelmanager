package ir.daneshrefah.scm.config.service;

import ir.daneshrefah.scm.config.Properties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
@Service
public class PropertyServicew {

    private JdbcTemplate jdbcTemplate;

    public PropertyServicew(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

//    @Cacheable(value = "cache_config", key = "#root.methodName + '-' + #application + '-' + #profile + '-' + #label")
    public List<Properties> find(String application, String profile, String label) {
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
            Properties property = new Properties(resultSet.getString("application_key"),
                    resultSet.getString("profile_key"),
                    resultSet.getString("label_key"),
                    resultSet.getString("prop_key"),
                    resultSet.getString("prop_value"));
            return property;
        }, args.toArray());
    }
}
