package ir.daneshrefah.scm.core.services;

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
public class TransformerService implements ir.daneshrefah.scm.plugin.api.service.TransformerService {

    @Autowired
    TransformerRepository transformerRepository;
    @Autowired
    TransformerRelationRepository transformerRelationRepository;

    @Override
    public List<Transformer> findAllTransformers() {
        return TransformerMapper.INSTANCE.entitiesToModels(transformerRepository.findAll());
    }

    @Override
    public List<TransformerRelation> findAllTransformerRelationsBySourceAndType(String sourceId,
                                                                                TransformerRelationType relationType) {
        return TransformerMapper.INSTANCE.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceIdAndRelationType(sourceId, relationType));
    }

    @Override
    public List<TransformerRelation> findAllTransformerRelationsBySource(String sourceId) {
        return TransformerMapper.INSTANCE.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceId(sourceId));
    }

}
