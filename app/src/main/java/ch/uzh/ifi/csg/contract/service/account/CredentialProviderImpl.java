package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.Credentials;

/**
 * Created by flo on 24.03.17.
 */

public class CredentialProviderImpl implements CredentialProvider {

    private Credentials credentials;

    public CredentialProviderImpl(Credentials credentials) {
        this.credentials = credentials;
    }

    public CredentialProviderImpl() {
    }

    @Override
    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }
}
