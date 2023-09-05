package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.mapper.TransformerMapper;
import ir.daneshrefah.scm.core.repository.TransformerRelationRepository;
import ir.daneshrefah.scm.core.repository.TransformerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransformerService {

    @Autowired
    TransformerRepository transformerRepository;
    @Autowired
    TransformerRelationRepository transformerRelationRepository;

    public List<Transformer> findAllTransformers() {
        return TransformerMapper.INSTANCE.entitiesToModels(transformerRepository.findAll());
    }

    public List<TransformerRelation> findAllTransformerRelationsBySourceAndType(String sourceId,
                                                                                TransformerRelationType relationType) {
        return TransformerMapper.INSTANCE.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceIdAndRelationType(sourceId, relationType));
    }

    public List<TransformerRelation> findAllTransformerRelationsBySource(String sourceId) {
        return TransformerMapper.INSTANCE.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceId(sourceId));
    }

}
