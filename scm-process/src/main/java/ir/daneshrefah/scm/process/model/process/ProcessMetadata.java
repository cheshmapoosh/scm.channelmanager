package ir.daneshrefah.scm.process.model.process;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data
public class ProcessMetadata implements BaseProcessModel {

    private String startValidationSchema;
    private String startValidationScript;
    /**
     * this property indicates which user authorities can start process
     * */
    private List<String> startAuthorizedAuthorities;
    /**
     * this property indicates which usernames can cancel process
     * */
    private List<String> cancelAuthorizedUsers;
    /**
     * this property indicates which user authorities can cancel process
     * */
    private List<String> cancelAuthorizedAuthorities;
    private Map<String, String> inputConverters;

}
