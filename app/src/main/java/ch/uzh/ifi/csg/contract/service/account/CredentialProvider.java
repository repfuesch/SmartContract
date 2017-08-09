package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.Credentials;

/**
 * Interface used to set and retrieve the {@link Credentials} of the currently unlocked account
 */
public interface CredentialProvider
{
    Credentials getCredentials();
    void setCredentials(Credentials credentials);
}
