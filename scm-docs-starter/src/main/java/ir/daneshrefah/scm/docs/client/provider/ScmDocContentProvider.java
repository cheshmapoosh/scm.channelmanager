package ir.daneshrefah.scm.docs.client.provider;

import ir.daneshrefah.scm.docs.client.model.ScmDocContent;

import java.util.Optional;

public interface ScmDocContentProvider {

    Optional<ScmDocContent> findById(String docId);
}
