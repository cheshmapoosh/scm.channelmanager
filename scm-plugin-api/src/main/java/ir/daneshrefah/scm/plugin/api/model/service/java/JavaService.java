package ir.daneshrefah.scm.plugin.api.model.service.java;

import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
@Setter
public class JavaService extends ScmService {

    private String javaImplementationClassName;
    private boolean implemented;
    private List<String> noneEditableProperties;

}
