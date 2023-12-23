package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractRemoteClientAuthenticationProvider extends AbstractClientAuthenticationProvider {

    protected final RemoteSecurityServiceProvider remoteSecurityServiceProvider;

    protected AbstractRemoteClientAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider) {
        this.remoteSecurityServiceProvider = remoteSecurityServiceProvider;
    }
}
