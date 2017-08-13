package smart_contract.csg.ifi.uzh.ch.smartcontract;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import ch.uzh.ifi.csg.contract.datamodel.Account;
import ch.uzh.ifi.csg.contract.service.account.AccountManager;
import ch.uzh.ifi.csg.contract.service.account.AccountService;
import ch.uzh.ifi.csg.contract.service.account.CredentialProvider;
import ch.uzh.ifi.csg.contract.service.account.WalletAccountService;
import ch.uzh.ifi.csg.contract.service.account.WalletWrapper;

/**
 * Unit-Tests for the {@link WalletAccountService} class
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    private static final String walletDir = "\\wallets";
    private static final String walletFile = "walletFile";
    private static final String testAccountPassword = "password";

    //mocks
    @Mock
    private Web3j web3;
    @Mock
    private AccountManager accountManager;
    @Mock
    private CredentialProvider credentialProvider;
    @Mock
    private WalletWrapper walletWrapper;
    @Mock
    private WalletAccountService testee;

    private Account testAccount;

    @Before
    public void setup()
    {
        testee = new WalletAccountService(web3, accountManager, credentialProvider, walletDir, false, walletWrapper);
        testAccount = new Account("id", "alias", testAccountPassword);
        testAccount.setWalletFile(walletFile);
    }

    /**
     * Checks that the specified accounts are returned by a call to
     * {@link AccountService#getAccounts()}
     */
    @Test
    public void getAccounts_whenCalled_thenAccountListReturned()
    {
        //arrange
        Account acc1 = new Account("id", "label", "walletfile");
        Account acc2 = new Account("id2", "label2", "walletfile2");
        Account[] accounts = new Account[] {acc1, acc2};
        List<Account> accountList = Arrays.asList(accounts);
        when(accountManager.getAccounts()).thenReturn(accountList);

        //act
        List<Account> result = testee.getAccounts().get();

        //assert
        assertThat(result, is(accountList));
    }

    /**
     * Checks that the Account is created and added to the accountManager
     * when calling {@link AccountService#createAccount}
     */
    @Test
    public void createAccount_whenCalled_thenAccountCreatedAndAdded() throws Exception
    {
        //arrange
        Credentials credentials = Mockito.mock(Credentials.class);
        when(credentials.getAddress()).thenReturn(testAccount.getId());
        when(walletWrapper.generateNewWalletFile(testAccountPassword, walletDir, false)).thenReturn(walletFile);
        when(walletWrapper.loadCredentials(testAccountPassword, walletDir + "/" + walletFile)).thenReturn(credentials);

        //act
        Account createdAccount = testee.createAccount(testAccount.getLabel(), testAccountPassword).get();

        //assert
        assert(createdAccount.getId().equals(testAccount.getId()));
        assert(createdAccount.getLabel().equals(testAccount.getLabel()));
        assert(createdAccount.getWalletFile()).equals(walletFile);
        verify(walletWrapper, times(1)).generateNewWalletFile(testAccountPassword, walletDir, false);
        verify(walletWrapper, times(1)).loadCredentials(testAccountPassword, walletDir + "/" + walletFile);
        verify(accountManager, times(1)).addAccount(createdAccount);
    }

    /**
     * Checks that a call to {@link AccountService#unlockAccount} sets the credentials when the
     * password for the account matches
     */
    @Test
    public void unlockAccount_whenCalledWithCorrectPassword_thenAccountUnlockedAndCredentialsProvided() throws Exception
    {
        //arrange
        Credentials credentials = Mockito.mock(Credentials.class);
        when(walletWrapper.loadCredentials(testAccountPassword, walletDir + "/" + walletFile)).thenReturn(credentials);

        //act
        Boolean result = testee.unlockAccount(testAccount, testAccountPassword).get();

        //assert
        assertThat(result, is(true));
        verify(credentialProvider, times(1)).setCredentials(credentials);
    }

    /**
     * Checks that a call to {@link AccountService#unlockAccount} returns 'false' when the password
     * for the account does not match
     */
    @Test
    public void unlockAccount_whenCalledWithWrongPassword_thenReturnFalse() throws Exception
    {
        //arrange
        Credentials credentials = Mockito.mock(Credentials.class);
        when(walletWrapper.loadCredentials(testAccountPassword, walletDir + "/" + walletFile)).thenThrow(new CipherException("error"));

        //act
        Boolean result = testee.unlockAccount(testAccount, testAccountPassword).get();

        //assert
        assertThat(result, is(false));
    }

    /**
     * Checks that the account balance for an account is returned when
     * {@link AccountService#getAccountBalance} is called
     */
    @Test
    public void getAccountBalance_whenCalled_thenReturnsAccountBalance() throws Exception
    {
        //arrange
        Request<?, EthGetBalance> fakeRequest = (Request<?, EthGetBalance>) Mockito.mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetBalance(testAccount.getId(), DefaultBlockParameterName.LATEST);
        EthGetBalance balanceResult = Mockito.mock(EthGetBalance.class);
        when(fakeRequest.send()).thenReturn(balanceResult);
        when(balanceResult.getBalance()).thenReturn(BigInteger.TEN);

        //act
        BigInteger result = testee.getAccountBalance(testAccount.getId()).get();

        //assert
        assertThat(result, is(BigInteger.TEN));
    }
}
