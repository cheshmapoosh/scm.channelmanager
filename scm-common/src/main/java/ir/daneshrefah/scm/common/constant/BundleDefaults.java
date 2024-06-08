package ir.daneshrefah.scm.common.constant;

public interface BundleDefaults {
    /* all bundles constants */
    /**
     * @implNote Inner resource bundle reference. (Automatically replaced on runtime)
     *
     * <pre>
     * <h1>Example :</h1>
     * <br>
     * bundleKey = x ,
     * <br>
     * bundleValue = ok!
     * <br><br>
     * bundleKey = y ,
     *  <br>
     * bundleValue = every thing will be @Ref::x::@
     *  <br><br>
     * 'y' key value on runtime = every thing will be ok!
     * </pre>
     */
    String INNER_REFERENCE_PREFIX = "@Ref::";
    String INNER_REFERENCE_SUFFIX = "::@";

    /* exception bundle constants */
    String EXCEPTION_BUNDLE_DEFAULT_PREFIX = "ex::";
    /**
     * @apiNote In some situation we need different exception handling for
     * a system exception. for example in we need to create two different Exception response
     * for {@link com.ibm.db2.jcc.am.SqlException}.
     * <pre>
     *     <br>
     *     exo::com.ibm.db2.jcc.am.SqlException::connection
     *     exo::com.ibm.db2.jcc.am.SqlException::resultSet
     *     <br>
     * </pre>
     */
    String EXCEPTION_BUNDLE_DEFAULT_PREFIX_OVERRIDEABLE = "exo::";
    String EXCEPTION_BUNDLE_DEFAULT_KEY = EXCEPTION_BUNDLE_DEFAULT_PREFIX + "default";
}
