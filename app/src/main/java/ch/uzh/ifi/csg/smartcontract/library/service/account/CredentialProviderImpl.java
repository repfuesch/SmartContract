package ch.uzh.ifi.csg.smartcontract.library.service.account;

import org.web3j.crypto.Credentials;

/**
 * CredentialProvider implementation
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
