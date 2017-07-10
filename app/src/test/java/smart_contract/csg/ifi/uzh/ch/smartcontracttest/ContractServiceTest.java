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
import java.util.ArrayList;

import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;
import ch.uzh.ifi.csg.contract.service.contract.ContractManager;
import ch.uzh.ifi.csg.contract.service.contract.Web3jContractService;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by flo on 10.07.17.
 */

@RunWith(MockitoJUnitRunner.class)
public class ContractServiceTest {

    private static final String transactionHash = "0xaeduhfiusdhgiudshgisduhgsg";

    @Mock
    private Web3j web3;

    private TransactionManager transactionManager;
    @Mock
    private ContractManager contractManager;
    @Mock
    private TransactionReceipt transactionReceipt;

    private Web3jContractService testee;

    private EthSendTransaction transactionResult;

    @Before
    public void setup()
    {
        transactionManager = new TestTransactionManager(web3, mock(Credentials.class), Byte.MIN_VALUE);
        testee = new Web3jContractService(web3,transactionManager,contractManager, BigInteger.TEN, BigInteger.TEN);
    }

    private void setupTransactionSuccess() throws Exception
    {
        transactionResult = mock(EthSendTransaction.class);
        //when(transactionManager.sendTransaction(any(BigInteger.class), any(BigInteger.class), any(String.class), any(String.class), any(BigInteger.class)))
                //.thenReturn(transactionResult);
        when(transactionResult.hasError()).thenReturn(false);
        when(transactionResult.getTransactionHash()).thenReturn(transactionHash);
        Request<?, EthGetTransactionReceipt> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetTransactionReceipt(transactionHash);
        EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);
        when(fakeRequest.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.hasError()).thenReturn(false);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(transactionReceipt);
    }

    @Test
    public void deployPurchaseContract_returnsPurchaseContract() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        //act
        IPurchaseContract result = (IPurchaseContract)testee.deployPurchaseContract(BigInteger.TEN, "title", "description", new ArrayList<String>(), false).get();

        //assert
        assertThat(result.getContractAddress(), is(contractAddress));
        assertThat(result.getContractType(), is(ContractType.Purchase));
    }

    @Test
    public void deployRentContract_returnsRentContract() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        //act
        IRentContract result = (IRentContract) testee.deployRentContract(BigInteger.TEN, BigInteger.TEN, TimeUnit.Days, "title", "description", new ArrayList<String>(), false).get();

        //assert
        assertThat(result.getContractAddress(), is(contractAddress));
        assertThat(result.getContractType(), is(ContractType.Rent));
    }

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

    @Test
    public void loadContract_WhenNewContract_thenContractSavedAndReturned() throws Exception
    {
        //arrange
        setupTransactionSuccess();
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        when(transactionReceipt.getContractAddress()).thenReturn(contractAddress);

        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";
        ContractInfo testContract = new ContractInfo(ContractType.Rent, contractAddress);
        when(contractManager.getContract(contractAddress, accountAddress)).thenReturn(null);

        //act
        IRentContract contract = (IRentContract) testee.loadContract(testContract.getContractType(), testContract.getContractAddress(), accountAddress).get();

        //assert
        assertThat(contract.getContractAddress(), is(testContract.getContractAddress()));
        assertThat(contract.getContractType(), is(testContract.getContractType()));
        verify(contractManager, times(1)).saveContract(any(ContractInfo.class), any(String.class));
    }

    @Test
    public void saveContract_WhenCalled_thenSavesContract()
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        ContractType type = ContractType.Purchase;
        String accountAddress = "0xaudhgfiuahgbpiudshgüpois8hdaosg";
        ArgumentCaptor<ContractInfo> argument1 = ArgumentCaptor.forClass(ContractInfo.class);
        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);
        
        //act
        testee.saveContract(contractAddress, type, accountAddress);
        
        //assert
        verify(contractManager, times(1)).saveContract(argument1.capture(), argument2.capture());
        assertThat(argument1.getValue().getContractType(), is(type));
        assertThat(argument1.getValue().getContractAddress(), is(contractAddress));
        assertThat(argument2.getValue(), is(accountAddress));
    }

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

    @Test
    public void isContract_WhenHasCode_ThenReturnTrue() throws Exception
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        EthGetCode ethCode = mock(EthGetCode.class);
        Request<?, EthGetCode> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetCode(contractAddress,  DefaultBlockParameterName.LATEST);
        when(fakeRequest.send()).thenReturn(ethCode);
        when(ethCode.getCode()).thenReturn("dsagsfdgsfgfdshfdh");

        //act
        boolean isContract = testee.isContract(contractAddress).get();

        //assert
        assertThat(isContract, is(true));
    }

    @Test
    public void isContract_WhenNotHasCode_ThenReturnFalse() throws Exception
    {
        //arrange
        String contractAddress = "0xaedgsdjghosiduhgfosihgdoishgfd";
        EthGetCode ethCode = mock(EthGetCode.class);
        Request<?, EthGetCode> fakeRequest = mock(Request.class);
        Mockito.doReturn(fakeRequest).when(web3).ethGetCode(contractAddress,  DefaultBlockParameterName.LATEST);
        when(fakeRequest.send()).thenReturn(ethCode);
        when(ethCode.getCode()).thenReturn("");

        //act
        boolean isContract = testee.isContract(contractAddress).get();

        //assert
        assertThat(isContract, is(false));
    }

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
