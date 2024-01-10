package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.common.model.terminal.Terminal;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Service
public class TerminalService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TerminalService(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Cacheable(value = "cache_terminal_list", key = "#root.methodName")
    public List<Terminal> findTerminalList() {
        List<Terminal> result = new ArrayList<>();
        return result;
    }

}
