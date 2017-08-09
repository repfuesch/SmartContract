package ch.uzh.ifi.csg.contract.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 * {@link IRentContract} implementation
 */
public class RentContract extends TradeContract implements IRentContract
{
    public static final String BINARY_FULL = "0x60606040523461000057604051610b04380380610b0483398101604090815281516020830151918301516060840151608085015160a0860151938601959485019492939190920191905b8585858484875b6002828155600384905560048054600160a060020a03191633600160a060020a031617905586516000805481805290927f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e56360206101006001851615026000190190931691909104601f908101839004820193928b01908390106100de57805160ff191683800117855561010b565b8280016001018555821561010b579182015b8281111561010b5782518255916020019190600101906100f0565b5b5061012c9291505b808211156101285760008155600101610114565b5090565b50506005805460a060020a60ff0219167401000000000000000000000000000000000000000086151502179055845160018054600082905290917fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf66020600261010085871615026000190190941693909304601f9081018490048201938a01908390106101c457805160ff19168380011785556101f1565b828001600101855582156101f1579182015b828111156101f15782518255916020019190600101906101d6565b5b506102129291505b808211156101285760008155600101610114565b5090565b50506007805460ff19169055805160068054828255600082905290917ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f91820191602085018215610281579160200282015b828111156102815782518255602090920191600190910190610264565b5b506102a29291505b808211156101285760008155600101610114565b5090565b50505b5050505050505b5050505050505b610842806102c26000396000f300606060405236156100b45763ffffffff60e060020a60003504166308551a5381146100b957806335a063b4146100e25780634a79d50c146100f15780637150d8ae1461017e5780637284e416146101a75780638500d668146102345780638687b4dd1461023e5780639a078f2c14610248578063a035b1fe146102b0578063b06a428a146102cf578063c0d047f0146102ee578063c19d93fb146102f8578063d0e30db014610326578063e8731c3214610345575b610000565b34610000576100c6610366565b60408051600160a060020a039092168252519081900360200190f35b34610000576100ef610375565b005b34610000576100fe6103e7565b604080516020808252835181830152835191928392908301918501908083838215610144575b80518252602083111561014457601f199092019160209182019101610124565b505050905090810190601f1680156101705780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576100c6610475565b60408051600160a060020a039092168252519081900360200190f35b34610000576100fe610484565b604080516020808252835181830152835191928392908301918501908083838215610144575b80518252602083111561014457601f199092019160209182019101610124565b505050905090810190601f1680156101705780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6100ef610511565b005b6100ef6105a4565b005b346100005761025561063b565b604080516020808252835181830152835191928392908301918581019102808383821561029d575b80518252602083111561029d57601f19909201916020918201910161027d565b5050509050019250505060405180910390f35b34610000576102bd61069d565b60408051918252519081900360200190f35b34610000576102bd6106a3565b60408051918252519081900360200190f35b6100ef6106eb565b005b34610000576103056107e6565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576102bd6107ef565b60408051918252519081900360200190f35b34610000576103526107f5565b604080519115158252519081900360200190f35b600454600160a060020a031681565b60045433600160a060020a0390811691161461039057610000565b60008060075460ff166003811161000057146103ab57610000565b6040517f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d990600090a16007805460ff191660021790555b5b505b565b6000805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561046d5780601f106104425761010080835404028352916020019161046d565b820191906000526020600020905b81548152906001019060200180831161045057829003601f168201915b505050505081565b600554600160a060020a031681565b60018054604080516020600284861615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561046d5780601f106104425761010080835404028352916020019161046d565b820191906000526020600020905b81548152906001019060200180831161045057829003601f168201915b505050505081565b60008060075460ff1660038111610000571461052c57610000565b60035434148061053b57610000565b426009556040517f7643bb5e7abbcc632d9d551d022d8946a16be804d7552c78f68ee3730b4b1e6a90600090a16005805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790556007805460ff191660011790555b5b505b50565b60045433600160a060020a039081169116146105bf57610000565b600160075460ff16600381116100005714806105e85750600360075460ff166003811161000057145b8015156105f457610000565b6105fc6106a3565b6008556040517f9910193657303db0842ab01c9d85764cedc6dff2431560f42e2372410b99932790600090a16007805460ff191660031790555b5b505b565b60408051602081810183526000825260068054845181840281018401909552808552929392909183018282801561069257602002820191906000526020600020905b8154815260019091019060200180831161067d575b505050505090505b90565b60025481565b600080600260075460ff16600381116100005714156106c65760085491506106e7565b60095415156106d857600091506106e7565b60095442039050806002540291505b5090565b600060038060075460ff1660038111610000571461070857610000565b60055433600160a060020a0390811691161461072357610000565b6008543410158061073357610000565b6040517e827a5d9c6c82c525d526c3b31d84505a0c0ae33166031de8e10594dd2f329c90600090a160078054600260ff19909116179055600854600554600354604051349093039550600160a060020a039091169190850180156108fc02916000818181858888f1935050505015806107d35750600454604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050155b156107dd57610000565b5b5b505b5b5050565b60075460ff1681565b60035481565b60055474010000000000000000000000000000000000000000900460ff16815600a165627a7a72305820213aebb13ecbc73caa31579c28120c6c7ccd2b1fbf4e0072e18a22b5edbaf48b0029";
    public static final String BINARY_LIGHT = "0x6060604052346100005760405160608061057d8339810160409081528151602083015191909201515b8282825b6000829055600183905560028054600160a060020a03191633600160a060020a031617905560048190556005805460ff191690555b5050505b5050505b610505806100786000396000f300606060405236156100935763ffffffff60e060020a60003504166308551a53811461009857806335a063b4146100c1578063646c2e33146100d05780637150d8ae146100ef5780638500d668146101185780638687b4dd14610122578063a035b1fe1461012c578063b06a428a1461014b578063c0d047f01461016a578063c19d93fb14610174578063d0e30db0146101a2575b610000565b34610000576100a56101c1565b60408051600160a060020a039092168252519081900360200190f35b34610000576100ce6101d0565b005b34610000576100dd610242565b60408051918252519081900360200190f35b34610000576100a5610248565b60408051600160a060020a039092168252519081900360200190f35b6100ce610257565b005b6100ce6102ea565b005b34610000576100dd610381565b60408051918252519081900360200190f35b34610000576100dd610387565b60408051918252519081900360200190f35b6100ce6103cf565b005b34610000576101816104ca565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576100dd6104d3565b60408051918252519081900360200190f35b600254600160a060020a031681565b60025433600160a060020a039081169116146101eb57610000565b60008060055460ff1660038111610000571461020657610000565b6040517f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d990600090a16005805460ff191660021790555b5b505b565b60045481565b600354600160a060020a031681565b60008060055460ff1660038111610000571461027257610000565b60015434148061028157610000565b426007556040517f7643bb5e7abbcc632d9d551d022d8946a16be804d7552c78f68ee3730b4b1e6a90600090a16003805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790556005805460ff191660011790555b5b505b50565b60025433600160a060020a0390811691161461030557610000565b600160055460ff166003811161000057148061032e5750600360055460ff166003811161000057145b80151561033a57610000565b610342610387565b6006556040517f9910193657303db0842ab01c9d85764cedc6dff2431560f42e2372410b99932790600090a16005805460ff191660031790555b5b505b565b60005481565b600080600260055460ff16600381116100005714156103aa5760065491506103cb565b60075415156103bc57600091506103cb565b60075442039050806000540291505b5090565b600060038060055460ff166003811161000057146103ec57610000565b60035433600160a060020a0390811691161461040757610000565b6006543410158061041757610000565b6040517e827a5d9c6c82c525d526c3b31d84505a0c0ae33166031de8e10594dd2f329c90600090a160058054600260ff19909116179055600654600354600154604051349093039550600160a060020a039091169190850180156108fc02916000818181858888f1935050505015806104b75750600254604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050155b156104c157610000565b5b5b505b5b5050565b60055460ff1681565b600154815600a165627a7a723058205b1a1a41aa1a1fce5550a144ee29d11d6553074cd429aef1fd017823c9e7e38f0029";

    protected RentContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    @Override
    public ContractType getContractType() {
        return ContractType.Rent;
    }

    @Override
    protected void registerContractEvents()
    {
        super.registerContractEvents();
        Event event = new Event("itemRented", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
        event = new Event("itemReturned", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
        event = new Event("paymentRequested", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
    }

    @Override
    public SimplePromise<String> returnItem() {

        return Async.toPromise(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception
                    {
                        BigInteger fee = getRentingFee().get();
                        Function function = new Function("returnItem", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                        TransactionReceipt result = executeTransaction(FunctionEncoder.encode(function), fee);
                        return result.getTransactionHash();
                    }
                });
    }

    @Override
    public SimplePromise<String> reclaimItem() {
        return Async.toPromise(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception
                    {
                        BigInteger deposit = getDeposit().get();
                        Function function = new Function("reclaimItem", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                        TransactionReceipt result = executeTransaction(FunctionEncoder.encode(function), deposit);
                        return result.getTransactionHash();
                    }
                });
    }

    @Override
    public SimplePromise<String> rentItem() {
        return Async.toPromise(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception
                    {
                        BigInteger deposit = getDeposit().get();
                        Function function = new Function("rentItem", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                        TransactionReceipt result = executeTransaction(FunctionEncoder.encode(function), deposit);
                        return result.getTransactionHash();
                    }
                });
    }

    @Override
    public SimplePromise<BigInteger> getRentingFee() {

        return Async.toPromise(new Callable<BigInteger>() {
            @Override
            public BigInteger call() throws Exception {
                Function function = new Function("calculateRentingFee",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {
                        }));
                Uint256 result = executeCallSingleValueReturn(function);

                return result.getValue();
            }
        });
    }
}
