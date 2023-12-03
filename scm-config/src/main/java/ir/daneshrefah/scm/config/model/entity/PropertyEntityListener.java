package ir.daneshrefah.scm.config.model.entity;

import ir.daneshrefah.scm.config.repository.PropertyHistoryRepository;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class PropertyEntityListener implements ApplicationContextAware {

    private ApplicationContext applicationContext;


    @PrePersist
    public void prePersist(PropertyEntity property) {
        logAction("save", property);
    }

    @PreUpdate
    public void preUpdate(PropertyEntity property) {
        logAction("update", property);
    }

    @PreRemove
    public void preRemove(PropertyEntity property) {
        logAction("delete", property);
    }

    private void logAction(String action, PropertyEntity property) {

        PropertyHistoryEntity history = new PropertyHistoryEntity();
        history.setAction(action);
        history.setPropertyId(property.getId());

        history.setApplicationId(property.getApplicationId());
        history.setProfileId(property.getProfileId());
        history.setLabelKey(property.getLabelKey());
        history.setPropValue(property.getPropValue());
        history.setPropKey(property.getPropKey());

        resolveRepository().save(history);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private PropertyHistoryRepository resolveRepository() {
        if (null != applicationContext) {
            return applicationContext.getBean(PropertyHistoryRepository.class);
        }
        return null;
    }
}
