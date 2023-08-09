package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.plugin.api.model.service.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class CompositionService extends Service {

    private List<ServiceRelation> relations;

    public List<ServiceRelation> getRelations() {
        return relations;
    }

    public void setRelations(List<ServiceRelation> relations) {
        this.relations = relations;
    }

    @Override
    public Object getServiceInfo() {
        return null;
    }
}
