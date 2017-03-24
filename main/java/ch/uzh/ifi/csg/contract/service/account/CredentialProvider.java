package ch.uzh.ifi.csg.contract.service.account;

import org.web3j.crypto.Credentials;

/**
 * Created by flo on 24.03.17.
 */

public interface CredentialProvider
{
    Credentials getCredentials();
    void setCredentials(Credentials credentials);
}
