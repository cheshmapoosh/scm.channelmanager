package ir.daneshrefah.scm.common.error.management;

import ir.daneshrefah.scm.common.model.error.ScmFault;

import java.util.Locale;

public interface ErrorHandler {

    boolean support(Exception exception);

    ScmFault handle(Exception exception, Locale locale);
}
