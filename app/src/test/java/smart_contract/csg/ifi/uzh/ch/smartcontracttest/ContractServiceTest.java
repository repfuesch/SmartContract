package smart_contract.csg.ifi.uzh.ch.smartcontracttest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.contract.ContractManager;
import ch.uzh.ifi.csg.contract.service.contract.ContractService;
import ch.uzh.ifi.csg.contract.service.contract.Web3jContractService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.transaction.TransactionHandler;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link Web3jContractService} class
 */
@RunWith(MockitoJUnitRunner.class)
public class ContractServiceTest {

    private static final String transactionHash = "0xaeduhfiusdhgiudshgisduhgsg";
    private static final String accountAddress = "0xadujgfhdsoaiughsoiuhgöosfdhgn";

    @Mock
    private Web3j web3;

    private TransactionManager transactionManager;
    @Mock
    private ContractManager contractManager;
    @Mock
    private TransactionReceipt transactionReceipt;
    @Mock
    private TransactionHandler transactionHandler;

    private Web3jContractService testee;

    private EthSendTransaction transactionResult;

    @Before
    public void setup()
    {
        transactionManager = new TestTransactionManager(web3, mock(Credentials.class), Byte.MIN_VALUE);
        testee = new Web3jContractService(web3,transactionManager,contractManager, transactionHandler, BigInteger.TEN, BigInteger.TEN, accountAddress);
    }

    /**
     * Makes sure that a transaction on the {@link TestTransactionManager}
     */
    private void setupTransactionSuccess() throws Exception
    {
        //fake transaction result
        transactionResult = mock(EthSendTransaction.class);
        when(transactionResult.hasError()).thenReturn(false);
        when(transactionResult.getTransactionHash()).thenReturn(transactionHash);

        //fake transaction receipt and makes sure that it doesn't have errors
        Request<?, EthGetTransactionReceipt> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetTransactionReceipt(transactionHash);
        EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);
        when(fakeRequest.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.hasError()).thenReturn(false);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(transactionReceipt);
    }

    /**
     * tests that a call to {@link ContractService#deployPurchaseContract}
     * returns an {@link IPurchaseContract} instance with the correct attributes
     */
    @Test
    public void deployPurchaseContract_returnsPurchaseContract() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        //act

        //use light deployment such that content attributes are retrieved locally
        IPurchaseContract result = testee.deployPurchaseContract(BigInteger.TEN, "title", "description", new HashMap<String, String>(), false, true).get();

        //assert
        assertThat(result.getContractAddress(), is(contractAddress));
        assertThat(result.getContractType(), is(ContractType.Purchase));
        assertThat(result.getTitle().get(), is("title"));
        assertThat(result.getDescription().get(), is("description"));
    }

    /**
     * tests that a call to {@link ContractService#deployRentContract}
     * returns an {@link IRentContract} instance with the correct attributes
     */
    @Test
    public void deployRentContract_returnsRentContract() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        //act
        IRentContract result = testee.deployRentContract(BigInteger.TEN, BigInteger.TEN, TimeUnit.Days, "title", "description", new HashMap<String, String>(), false, true).get();

        //assert
        assertThat(result.getContractAddress(), is(contractAddress));
        assertThat(result.getContractType(), is(ContractType.Rent));
        assertThat(result.getTitle().get(), is("title"));
        assertThat(result.getDescription().get(), is("description"));
    }

    /**
     * Checks that an existing contract is loaded when {@link ContractService#loadContract}
     * is called
     */
    @Test
    public void loadContract_WhenExistingContract_thenContractReturned() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";
        ContractInfo testContract = new ContractInfo(ContractType.Purchase, contractAddress);
        when(contractManager.getContract(contractAddress, accountAddress)).thenReturn(testContract);

        //act
        IPurchaseContract contract = (IPurchaseContract)testee.loadContract(testContract.getContractType(), testContract.getContractAddress(), accountAddress).get();

        //assert
        assertThat(contract.getContractAddress(), is(testContract.getContractAddress()));
        assertThat(contract.getContractType(), is(testContract.getContractType()));
    }

    /**
     * Checks that {@link ContractService#loadContract} returns null when a contract is requested
     * that is not saved
     */
    @Test
    public void loadContract_WhenContractNotSaved_thenReturnNull() throws Exception
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";
        ContractInfo testContract = new ContractInfo(ContractType.Rent, contractAddress);
        when(contractManager.getContract(contractAddress, accountAddress)).thenReturn(null);

        //act
        IRentContract contract = (IRentContract) testee.loadContract(testContract.getContractType(), testContract.getContractAddress(), accountAddress).get();

        //assert
        assertNull(contract);
    }

    /**
     * Checks that a call to {@link ContractService#saveContract(ContractInfo, String)} invokes the
     * contractManager with the correct arguments
     */
    @Test
    public void saveContract_WhenCalled_thenSavesContract()
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        ContractType type = ContractType.Purchase;
        ContractInfo info = new ContractInfo(type, contractAddress);

        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";
        ArgumentCaptor<ContractInfo> argument1 = ArgumentCaptor.forClass(ContractInfo.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        
        //act
        testee.saveContract(info, accountAddress);
        
        //assert
        verify(contractManager, times(1)).saveContract(argument1.capture(), argument2.capture());
        assertThat(argument1.getValue(), is(info));
        assertThat(argument2.getValue(), is(accountAddress));
    }

    /**
     * Checks that a call to {@link ContractService#removeContract(ITradeContract, String)} invokes the
     * contractManager with the correct arguments
     */
    @Test
    public void removeContract_WhenCalled_thenRemovesContract()
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";

        //act
        testee.removeContract(contractAddress, accountAddress);

        //assert
        verify(contractManager, times(1)).deleteContract(contractAddress, accountAddress);
    }

    /**
     * Checks that a call to {@link ContractService#verifyContractCode(String, String)} returns
     * 'true' when the contract at the specified address contains the specified binary code
     */
    @Test
    public void verifyContract_WhenHasCode_ThenReturnTrue() throws Exception
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        String contractCode = "dsagsfdgsfgfdshfdh";

        //fake eth call
        EthGetCode ethCode = mock(EthGetCode.class);
        Request<?, EthGetCode> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetCode(contractAddress,  DefaultBlockParameterName.LATEST);
        when(fakeRequest.send()).thenReturn(ethCode);
        when(ethCode.getCode()).thenReturn("dsagsfdgsfgfdshfdh");

        //act
        boolean valid = testee.verifyContractCode(contractAddress, contractCode).get();

        //assert
        assertThat(valid, is(true));
    }

    /**
     * Checks that a call to {@link ContractService#verifyContractCode(String, String)} returns
     * 'false' when the contract at the specified address does not contain the specified binary code
     */
    @Test
    public void verifyContract_WhenNotHasCode_ThenReturnFalse() throws Exception
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        String contractCode = "dsagsfdgsfgfdshfdh";

        //fake eth call
        EthGetCode ethCode = mock(EthGetCode.class);
        Request<?, EthGetCode> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetCode(contractAddress,  DefaultBlockParameterName.LATEST);
        when(fakeRequest.send()).thenReturn(ethCode);
        when(ethCode.getCode()).thenReturn("aedgsgrgrgedhhedhrt");

        //act
        boolean valid = testee.verifyContractCode(contractAddress, contractCode).get();

        //assert
        assertThat(valid, is(false));
    }

    /**
     * Test {@link TransactionManager} that always returns the same transactionResult mock
     * object in its {@link TransactionManager#sendTransaction} method.
     *
     */
    public class TestTransactionManager extends RawTransactionManager {
        public TestTransactionManager(Web3j web3j, Credentials credentials, byte chainId) {
            super(web3j, credentials, chainId);
        }

        @Override
        public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
            return transactionResult;
        }
    }
}
