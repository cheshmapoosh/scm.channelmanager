package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.model.ScmApiDocGroupDescriptor;

import java.util.Collection;

public interface ScmApiDocGroupCatalogProvider {

    Collection<ScmApiDocGroupDescriptor> findApiDocGroups();
}
