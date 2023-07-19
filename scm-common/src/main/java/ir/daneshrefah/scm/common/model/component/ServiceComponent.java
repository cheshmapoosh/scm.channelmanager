package ir.daneshrefah.scm.common.model.component;

import ir.daneshrefah.scm.common.model.BaseModel;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class ServiceComponent extends BaseModel {

    private String code;
    private String title;
    private ServiceComponentProvider provider;
    private String requestJSONSchema;
    private String responseJSONSchema;
    private String metadata;


}
