package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.model.ScmDocDescriptor;

import java.util.Collection;

public interface ScmDocCatalogProvider {

    Collection<ScmDocDescriptor> findAll();
}
