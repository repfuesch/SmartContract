package ch.uzh.ifi.csg.contract.contract;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.MessageDecodingException;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import ch.uzh.ifi.csg.contract.async.Async;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;

/**
 *
 * {@link IPurchaseContract} implementation
 */
public class PurchaseContract extends TradeContract implements IPurchaseContract {

    public static final String BINARY_FULL = "0x60606040526040516109ff3803806109ff8339810160409081528151602083015191830151606084015191840193928301929091015b8383836002340480855b6003839055600282815560048054600160a060020a03191633600160a060020a031617905586516000805481805290927f290decd9548b62a8d60345a988386fc84ba6bc95484008f6362f93160ef3e56360206101006001851615026000190190931691909104601f908101839004820193928b01908390106100cd57805160ff19168380011785556100fa565b828001600101855582156100fa579182015b828111156100fa5782518255916020019190600101906100df565b5b5061011b9291505b808211156101175760008155600101610103565b5090565b50506005805460a060020a60ff0219167401000000000000000000000000000000000000000086151502179055845160018054600082905290917fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf66020600261010085871615026000190190941693909304601f9081018490048201938a01908390106101b357805160ff19168380011785556101e0565b828001600101855582156101e0579182015b828111156101e05782518255916020019190600101906101c5565b5b506102019291505b808211156101175760008155600101610103565b5090565b50506007805460ff19169055805160068054828255600082905290917ff652222313e28459528d920b65115c16c04f3efc82aaedc97be59f3f377c0d3f91820191602085018215610270579160200282015b828111156102705782518255602090920191600190910190610253565b5b506102919291505b808211156101175760008155600101610103565b5090565b50505b505050505050346002546002021415156102ad57610000565b5b505050505b61073d806102c26000396000f3006060604052361561009e5763ffffffff60e060020a60003504166308551a5381146100a357806335a063b4146100cc5780634a79d50c146100db5780637150d8ae146101685780637284e4161461019157806373fac6f01461021e5780639a078f2c1461022d578063a035b1fe14610295578063c19d93fb146102b4578063d0e30db0146102e2578063d696069714610301578063e8731c321461030b575b610000565b34610000576100b061032c565b60408051600160a060020a039092168252519081900360200190f35b34610000576100d961033b565b005b34610000576100e86103e4565b60408051602080825283518183015283519192839290830191850190808383821561012e575b80518252602083111561012e57601f19909201916020918201910161010e565b505050905090810190601f16801561015a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576100b0610472565b60408051600160a060020a039092168252519081900360200190f35b34610000576100e8610481565b60408051602080825283518183015283519192839290830191850190808383821561012e575b80518252602083111561012e57601f19909201916020918201910161010e565b505050905090810190601f16801561015a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576100d961050e565b005b346100005761023a6105e8565b6040805160208082528351818301528351919283929083019185810191028083838215610282575b80518252602083111561028257601f199092019160209182019101610262565b5050509050019250505060405180910390f35b34610000576102a261064a565b60408051918252519081900360200190f35b34610000576102c1610650565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576102a2610659565b60408051918252519081900360200190f35b6100d961065f565b005b34610000576103186106f0565b604080519115158252519081900360200190f35b600454600160a060020a031681565b60045433600160a060020a0390811691161461035657610000565b60008060075460ff1660038111610000571461037157610000565b6040517f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d990600090a160078054600260ff19909116179055600454604051600160a060020a039182169130163180156108fc02916000818181858888f1935050505015156103de57610000565b5b5b505b565b6000805460408051602060026001851615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561046a5780601f1061043f5761010080835404028352916020019161046a565b820191906000526020600020905b81548152906001019060200180831161044d57829003601f168201915b505050505081565b600554600160a060020a031681565b60018054604080516020600284861615610100026000190190941693909304601f8101849004840282018401909252818152929183018282801561046a5780601f1061043f5761010080835404028352916020019161046a565b820191906000526020600020905b81548152906001019060200180831161044d57829003601f168201915b505050505081565b60055433600160a060020a0390811691161461052957610000565b60018060075460ff1660038111610000571461054457610000565b6040517f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d90600090a16007805460ff19166002179055600554600354604051600160a060020a03909216916108fc82150291906000818181858888f1935050505015806105d85750600454604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050155b156103de57610000565b5b5b505b565b60408051602081810183526000825260068054845181840281018401909552808552929392909183018282801561063f57602002820191906000526020600020905b8154815260019091019060200180831161062a575b505050505090505b90565b60025481565b60075460ff1681565b60035481565b60008060075460ff1660038111610000571461067a57610000565b600280540234148061068b57610000565b6040517f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b490600090a16005805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790556007805460ff191660011790555b5b505b50565b60055474010000000000000000000000000000000000000000900460ff16815600a165627a7a72305820cd6261fb1b5579d2d38c0ddc2e8516782ff27d5f27be5932689c1356a38363300029";
    public static final String BINARY_LIGHT = "0x606060405260405160208061048183398101604052515b6002340480825b6000829055600183905560028054600160a060020a03191633600160a060020a031617905560048190556005805460ff191690555b50506000546002023414905061006757610000565b5b505b610408806100796000396000f3006060604052361561007d5763ffffffff60e060020a60003504166308551a53811461008257806335a063b4146100ab578063646c2e33146100ba5780637150d8ae146100d957806373fac6f014610102578063a035b1fe14610111578063c19d93fb14610130578063d0e30db01461015e578063d69606971461017d575b610000565b346100005761008f610187565b60408051600160a060020a039092168252519081900360200190f35b34610000576100b8610196565b005b34610000576100c7610244565b60408051918252519081900360200190f35b346100005761008f61024a565b60408051600160a060020a039092168252519081900360200190f35b34610000576100b8610259565b005b34610000576100c7610333565b60408051918252519081900360200190f35b346100005761013d610339565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576100c7610342565b60408051918252519081900360200190f35b6100b8610348565b005b600254600160a060020a031681565b60025433600160a060020a039081169116146101b157610000565b60008060055460ff166003811161000057146101cc57610000565b6040517f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d990600090a1600580546002919060ff19166001830217905550600254604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050151561023e57610000565b5b5b505b565b60045481565b600354600160a060020a031681565b60035433600160a060020a0390811691161461027457610000565b60018060055460ff1660038111610000571461028f57610000565b6040517f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d90600090a16005805460ff19166002179055600354600154604051600160a060020a03909216916108fc82150291906000818181858888f1935050505015806103235750600254604051600160a060020a039182169130163180156108fc02916000818181858888f19350505050155b1561023e57610000565b5b5b505b565b60005481565b60055460ff1681565b60015481565b60008060055460ff1660038111610000571461036357610000565b600054600202341480151561037757610000565b6040517f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b490600090a16003805473ffffffffffffffffffffffffffffffffffffffff191633600160a060020a03161790556005805460ff191660011790555b5b505b505600a165627a7a72305820352bf8a981f1ee8b8005da80bfaf46c11775fb4efa53bcc9c13296cdb232682d0029";

    private PurchaseContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit)
    {
        super(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public SimplePromise<String> confirmReceived() {
        return Async.toPromise(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Function function = new Function("confirmReceived", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                TransactionReceipt result = executeTransaction(function);
                return result.getTransactionHash();
            }
        });
    }

    public SimplePromise<String> confirmPurchase() {
        return Async.toPromise(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Function function = new Function("confirmPurchase", Arrays.<Type>asList(), Collections.<TypeReference<?>>emptyList());
                        TransactionReceipt result = executeTransaction(FunctionEncoder.encode(function), getPrice().get().multiply(BigInteger.valueOf(2)));
                        return result.getTransactionHash();
                    }
                });
    }

    protected void registerContractEvents()
    {
        super.registerContractEvents();

        Event event = new Event("purchaseConfirmed", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
        event = new Event("itemReceived", new ArrayList<TypeReference<?>>(), new ArrayList<TypeReference<?>>());
        registerEvent(event);
    }

    @Override
    public ContractType getContractType() {
        return ContractType.Purchase;
    }
}
