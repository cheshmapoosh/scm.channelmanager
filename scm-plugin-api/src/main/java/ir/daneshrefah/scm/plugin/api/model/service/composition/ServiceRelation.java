package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Data
public class ServiceRelation extends BaseModel<String> {

    private Service sourceService;
    private Integer order;
    private Service targetService;
    private List<TransformerRelation> targetServiceRequestTransformers;
    private List<TransformerRelation> targetServiceResponseTransformers;
    private Service targetServiceCommit;
    private List<TransformerRelation> targetServiceCommitRequestTransformers;
    private List<TransformerRelation> targetServiceCommitResponseTransformers;
    private Service targetServiceReverse;
    private List<TransformerRelation> targetServiceReverseRequestTransformers;
    private List<TransformerRelation> targetServiceReverseResponseTransformers;

}
