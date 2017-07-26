package ch.uzh.ifi.csg.contract.contract;

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

public class PurchaseContract extends TradeContract implements IPurchaseContract {

    public static final String BINARY_FULL = "0x606060405260405162000dd838038062000dd8833981016040528080518201919060200180518201919060200180519060200190919080518201919050505b83838360023481156200000057046002348115620000005704855b816002819055508260038190555033600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508560009080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620000f657805160ff191683800117855562000127565b8280016001018555821562000127579182015b828111156200012657825182559160200191906001019062000109565b5b5090506200014f91905b808211156200014b57600081600090555060010162000131565b5090565b505083600560146101000a81548160ff0219169083151502179055508460019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001b957805160ff1916838001178555620001ea565b82800160010185558215620001ea579182015b82811115620001e9578251825591602001919060010190620001cc565b5b5090506200021291905b808211156200020e576000816000905550600101620001f4565b5090565b50506000600760006101000a81548160ff0219169083600381116200000057021790555062000255816200027c64010000000002620009db176401000000009004565b5b50505050505034600254600202141515620002715762000000565b5b5050505062000322565b6000600090505b81518110156200031d5760068054806001018281815481835581811511620002da57818360005260206000209182019101620002d991905b80821115620002d5576000816000905550600101620002bb565b5090565b5b505050916000526020600020900160005b84848151811015620000005790602001906020020151909190915090600019169055505b808060010191505062000283565b5b5050565b610aa680620003326000396000f300606060405236156100b8576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a53146100bd57806335a063b41461010c5780634a79d50c1461011b5780637150d8ae146101b15780637284e4161461020057806373fac6f0146102965780639a078f2c146102a5578063a035b1fe14610317578063c19d93fb1461033a578063d0e30db014610368578063d69606971461038b578063e8731c3214610395575b610000565b34610000576100ca6103bc565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576101196103e2565b005b346100005761012861053f565b6040518080602001828103825283818151815260200191508051906020019080838360008314610177575b80518252602083111561017757602082019150602081019050602083039250610153565b505050905090810190601f1680156101a35780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576101be6105dd565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761020d610603565b604051808060200182810382528381815181526020019150805190602001908083836000831461025c575b80518252602083111561025c57602082019150602081019050602083039250610238565b505050905090810190601f1680156102885780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576102a36106a1565b005b34610000576102b2610862565b6040518080602001828103825283818151815260200191508051906020019060200280838360008314610304575b805182526020831115610304576020820191506020810190506020830392506102e0565b5050509050019250505060405180910390f35b34610000576103246108ce565b6040518082815260200191505060405180910390f35b34610000576103476108d4565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576103756108e7565b6040518082815260200191505060405180910390f35b6103936108ed565b005b34610000576103a26109c8565b604051808215151515815260200191505060405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561043e57610000565b6000806003811161000057600760009054906101000a900460ff16600381116100005714151561046d57610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600760006101000a81548160ff021916908360038111610000570217905550600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050151561053957610000565b5b5b505b565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105d55780601f106105aa576101008083540402835291602001916105d5565b820191906000526020600020905b8154815290600101906020018083116105b857829003601f168201915b505050505081565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106995780601f1061066e57610100808354040283529160200191610699565b820191906000526020600020905b81548152906001019060200180831161067c57829003601f168201915b505050505081565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156106fd57610000565b6001806003811161000057600760009054906101000a900460ff16600381116100005714151561072c57610000565b7f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d60405180905060405180910390a16002600760006101000a81548160ff021916908360038111610000570217905550600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc6003549081150290604051809050600060405180830381858888f1935050505015806108525750600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b1561085c57610000565b5b5b505b565b602060405190810160405280600081525060068054806020026020016040519081016040528092919081815260200182805480156108c357602002820191906000526020600020905b815460001916815260200190600101908083116108ab575b505050505090505b90565b60025481565b600760009054906101000a900460ff1681565b60035481565b6000806003811161000057600760009054906101000a900460ff16600381116100005714151561091c57610000565b600254600202341480151561093057610000565b7f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b460405180905060405180910390a133600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600760006101000a81548160ff0219169083600381116100005702179055505b5b505b50565b600560149054906101000a900460ff1681565b6000600090505b8151811015610a755760068054806001018281815481835581811511610a3457818360005260206000209182019101610a3391905b80821115610a2f576000816000905550600101610a17565b5090565b5b505050916000526020600020900160005b848481518110156100005790602001906020020151909190915090600019169055505b80806001019150506109e2565b5b50505600a165627a7a72305820bcdb5fb3a43824011ca269b3c031313039fc0b8207ae8aeb303aba04f24b52500029";
    public static final String BINARY_LIGHT = "0x6060604052604051602080610772833981016040528080519060200190919050505b6002348115610000570460023481156100005704825b816000819055508260018190555033600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600481600019169055506000600560006101000a81548160ff0219169083600381116100005702179055505b505050346000546002021415156100c957610000565b5b505b610697806100db6000396000f30060606040523615610097576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a531461009c57806335a063b4146100eb578063646c2e33146100fa5780637150d8ae1461012557806373fac6f014610174578063a035b1fe14610183578063c19d93fb146101a6578063d0e30db0146101d4578063d6960697146101f7575b610000565b34610000576100a9610201565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576100f8610227565b005b3461000057610107610384565b60405180826000191660001916815260200191505060405180910390f35b346100005761013261038a565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576101816103b0565b005b3461000057610190610571565b6040518082815260200191505060405180910390f35b34610000576101b3610577565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576101e161058a565b6040518082815260200191505060405180910390f35b6101ff610590565b005b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561028357610000565b6000806003811161000057600560009054906101000a900460ff1660038111610000571415156102b257610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600560006101000a81548160ff021916908360038111610000570217905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050151561037e57610000565b5b5b505b565b60045481565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561040c57610000565b6001806003811161000057600560009054906101000a900460ff16600381116100005714151561043b57610000565b7f64ea507aa320f07ae13c28b5e9bf6b4833ab544315f5f2aa67308e21c252d47d60405180905060405180910390a16002600560006101000a81548160ff021916908360038111610000570217905550600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc6001549081150290604051809050600060405180830381858888f1935050505015806105615750600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b1561056b57610000565b5b5b505b565b60005481565b600560009054906101000a900460ff1681565b60015481565b6000806003811161000057600560009054906101000a900460ff1660038111610000571415156105bf57610000565b60005460020234148015156105d357610000565b7f764326667cab2f2f13cad5f7b7665c704653bd1acc250dcb7b422bce726896b460405180905060405180910390a133600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600560006101000a81548160ff0219169083600381116100005702179055505b5b505b505600a165627a7a723058205ef5ae9c2d9b815a327d651c57bb1ffc81e04939f663c043e82f6c41b353e1750029";

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
                        try{
                            TransactionReceipt result = executeTransaction(function, getPrice().get().multiply(BigInteger.valueOf(2)));
                            return result.getTransactionHash();
                        }catch(MessageDecodingException ex)
                        {
                            ex.printStackTrace();
                            return null;
                        }
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
