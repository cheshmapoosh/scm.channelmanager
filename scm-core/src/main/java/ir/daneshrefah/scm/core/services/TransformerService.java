package ir.daneshrefah.scm.core.services;

import ir.daneshrefah.scm.common.model.transformer.Transformer;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.mapper.TransformerMapper;
import ir.daneshrefah.scm.core.repository.TransformerRelationRepository;
import ir.daneshrefah.scm.core.repository.TransformerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransformerService implements ir.daneshrefah.scm.plugin.api.service.TransformerService {

    private final TransformerRepository transformerRepository;
    private final TransformerRelationRepository transformerRelationRepository;
    private final TransformerMapper transformerMapper;

    @Override
    public List<Transformer> findAllTransformers() {
        return transformerMapper.entitiesToModels(transformerRepository.findAll());
    }

    @Override
    public List<TransformerRelation> findAllTransformerRelationsBySourceAndType(String sourceId,
                                                                                TransformerRelationType relationType) {
        return transformerMapper.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceIdAndRelationType(sourceId, relationType));
    }

    @Override
    public List<TransformerRelation> findAllTransformerRelationsBySource(String sourceId) {
        return transformerMapper.entitiesToRelationModels(
                transformerRelationRepository.findAllBySourceId(sourceId));
    }

}
