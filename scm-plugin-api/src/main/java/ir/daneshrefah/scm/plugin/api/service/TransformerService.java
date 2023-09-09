package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-09-09
 */
public interface TransformerService {

    public List<Transformer> findAllTransformers();

    public List<TransformerRelation> findAllTransformerRelationsBySourceAndType(String sourceId,
                                                                                TransformerRelationType relationType);

    public List<TransformerRelation> findAllTransformerRelationsBySource(String sourceId);

}
