package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.Credentials;

/**
 * Interface for retrieving and setting the active credentials of an account
 */

public interface CredentialProvider
{
    Credentials getCredentials();
    void setCredentials(Credentials credentials);
}
