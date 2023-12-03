package ir.daneshrefah.scm.config.service;

import ir.daneshrefah.scm.config.model.property.EnvironmentProperty;
import ir.daneshrefah.scm.config.model.property.Property;
import ir.daneshrefah.scm.config.model.property.PropertyEditDTO;
import ir.daneshrefah.scm.config.model.mapper.PropertyMapper;
import ir.daneshrefah.scm.config.repository.ApplicationRepository;
import ir.daneshrefah.scm.config.repository.ProfileRepository;
import ir.daneshrefah.scm.config.repository.PropertyRepository;
import ir.daneshrefah.scm.config.model.property.PropertyDTO;
import ir.daneshrefah.scm.config.model.entity.PropertyEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, ProfileRepository profileRepository, ApplicationRepository applicationRepository) {
        this.propertyRepository = propertyRepository;
        this.profileRepository = profileRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<EnvironmentProperty> findProperties(String applicationCode, String profileCode, String label) {

        String applicationId = applicationRepository.findByCode(applicationCode).getId();
        String profileId = profileRepository.findByCode(profileCode).getId();
        List<PropertyEntity> propertyEntities = propertyRepository.findPropertyEntitiesByApplicationIdAndProfileIdAndLabelKey(applicationId,profileId,label);
        return PropertyMapper.INSTANCE.toPropertiesModel(propertyEntities);
    }

    public List<PropertyDTO> findAllProperties() {
        return PropertyMapper.INSTANCE.toPropertiesDTO(propertyRepository.findAll());
    }

    public PropertyDTO findPropertyById(String id) {
        return PropertyMapper.INSTANCE.toPropertyDTO(propertyRepository.findById(id));
    }

    public void deletePropertyById(String id) {
        propertyRepository.deleteById(id);
    }

    public void addNewProperty(Property propertyDTO) {
        PropertyEntity propertyEntity = PropertyMapper.INSTANCE.toPropertyEntity(propertyDTO);
        propertyRepository.save(propertyEntity);
    }

    public void editProperty(String id, PropertyEditDTO propertyDTO) {
        Optional<PropertyEntity> property = propertyRepository.findById(id);
        if (property.isPresent()) {
            PropertyEntity propertyEntity = PropertyMapper.INSTANCE.toEditPropertyEntity(propertyDTO);
            propertyEntity.setId(id);
            propertyEntity.setApplicationId(property.get().getApplicationId());
            propertyEntity.setProfileId(property.get().getProfileId());
            propertyRepository.save(propertyEntity);
        }
    }

    public List<PropertyDTO>  getPropertyByProfileIdAndApplicationId(String profileId, String applicationId) {

        List<PropertyEntity> propertyEntities = propertyRepository.findPropertyEntitiesByProfileIdAndApplicationId(profileId,applicationId);
        return PropertyMapper.INSTANCE.toPropertiesDTO(propertyEntities);
    }
}
