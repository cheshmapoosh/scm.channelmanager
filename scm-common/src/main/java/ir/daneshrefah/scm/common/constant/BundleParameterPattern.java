package ir.daneshrefah.scm.common.constant;

public enum BundleParameterPattern {
    /**
     * Like : username %s is invalid
     */
    STRING_FORMAT,
    /**
     * Like : username {0} is invalid
     */
    BRACKET_SERIES,
    /**
     * Like : username :username is invalid
     */
    KEY_ASSIGNMENT

}
