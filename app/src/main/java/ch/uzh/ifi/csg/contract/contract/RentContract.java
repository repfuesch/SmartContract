package ch.uzh.ifi.csg.contract.contract;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
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
 * Created by flo on 03.06.17.
 */

public class RentContract extends TradeContract implements IRentContract
{
    public static final String BINARY = "0x606060405234620000005760405162000f2738038062000f27833981016040528080518201919060200180518201919060200180519060200190919080518201919060200180519060200190919080519060200190919050505b8585858484875b816002819055508260038190555033600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508560009080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620000fd57805160ff19168380011785556200012e565b828001600101855582156200012e579182015b828111156200012d57825182559160200191906001019062000110565b5b5090506200015691905b808211156200015257600081600090555060010162000138565b5090565b505083600560146101000a81548160ff0219169083151502179055508460019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001c057805160ff1916838001178555620001f1565b82800160010185558215620001f1579182015b82811115620001f0578251825591602001919060010190620001d3565b5b5090506200021991905b8082111562000215576000816000905550600101620001fb565b5090565b50506000600760006101000a81548160ff021916908360038111620000005702179055506200025c81620002706401000000000262000b36176401000000009004565b5b5050505050505b50505050505062000316565b6000600090505b8151811015620003115760068054806001018281815481835581811511620002ce57818360005260206000209182019101620002cd91905b80821115620002c9576000816000905550600101620002af565b5090565b5b505050916000526020600020900160005b84848151811015620000005790602001906020020151909190915090600019169055505b808060010191505062000277565b5b5050565b610c0180620003266000396000f300606060405236156100ce576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a53146100d357806335a063b4146101225780634a79d50c146101315780637150d8ae146101c75780637284e416146102165780638500d668146102ac5780638687b4dd146102b65780639a078f2c146102c0578063a035b1fe14610332578063b06a428a14610355578063c0d047f014610378578063c19d93fb14610382578063d0e30db0146103b0578063e8731c32146103d3575b610000565b34610000576100e06103fa565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761012f610420565b005b346100005761013e610501565b604051808060200182810382528381815181526020019150805190602001908083836000831461018d575b80518252602083111561018d57602082019150602081019050602083039250610169565b505050905090810190601f1680156101b95780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34610000576101d461059f565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34610000576102236105c5565b6040518080602001828103825283818151815260200191508051906020019080838360008314610272575b8051825260208311156102725760208201915060208101905060208303925061024e565b505050905090810190601f16801561029e5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6102b4610663565b005b6102be610742565b005b34610000576102cd61085b565b604051808060200182810382528381815181526020019150805190602001906020028083836000831461031f575b80518252602083111561031f576020820191506020810190506020830392506102fb565b5050509050019250505060405180910390f35b346100005761033f6108c7565b6040518082815260200191505060405180910390f35b34610000576103626108cd565b6040518082815260200191505060405180910390f35b61038061092a565b005b346100005761038f610b0a565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576103bd610b1d565b6040518082815260200191505060405180910390f35b34610000576103e0610b23565b604051808215151515815260200191505060405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561047c57610000565b6000806003811161000057600760009054906101000a900460ff1660038111610000571415156104ab57610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600760006101000a81548160ff0219169083600381116100005702179055505b5b505b565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105975780601f1061056c57610100808354040283529160200191610597565b820191906000526020600020905b81548152906001019060200180831161057a57829003601f168201915b505050505081565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561065b5780601f106106305761010080835404028352916020019161065b565b820191906000526020600020905b81548152906001019060200180831161063e57829003601f168201915b505050505081565b6000806003811161000057600760009054906101000a900460ff16600381116100005714151561069257610000565b60035434148015156106a357610000565b426009819055507f7643bb5e7abbcc632d9d551d022d8946a16be804d7552c78f68ee3730b4b1e6a60405180905060405180910390a133600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600760006101000a81548160ff0219169083600381116100005702179055505b5b505b50565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561079e57610000565b60016003811161000057600760009054906101000a900460ff16600381116100005714806107eb575060036003811161000057600760009054906101000a900460ff166003811161000057145b8015156107f757610000565b6107ff6108cd565b6008819055507f9910193657303db0842ab01c9d85764cedc6dff2431560f42e2372410b99932760405180905060405180910390a16003600760006101000a81548160ff0219169083600381116100005702179055505b5b505b565b602060405190810160405280600081525060068054806020026020016040519081016040528092919081815260200182805480156108bc57602002820191906000526020600020905b815460001916815260200190600101908083116108a4575b505050505090505b90565b60025481565b6000600060026003811161000057600760009054906101000a900460ff1660038111610000571415610903576008549150610926565b600060095414156109175760009150610926565b60095442039050806002540291505b5090565b60006003806003811161000057600760009054906101000a900460ff16600381116100005714151561095b57610000565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156109b757610000565b6008543410158015156109c957610000565b7e827a5d9c6c82c525d526c3b31d84505a0c0ae33166031de8e10594dd2f329c60405180905060405180910390a16002600760006101000a81548160ff02191690836003811161000057021790555034600854039250600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc84600354019081150290604051809050600060405180830381858888f193505050501580610af75750600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b15610b0157610000565b5b5b505b5b5050565b600760009054906101000a900460ff1681565b60035481565b600560149054906101000a900460ff1681565b6000600090505b8151811015610bd05760068054806001018281815481835581811511610b8f57818360005260206000209182019101610b8e91905b80821115610b8a576000816000905550600101610b72565b5090565b5b505050916000526020600020900160005b848481518110156100005790602001906020020151909190915090600019169055505b8080600101915050610b3d565b5b50505600a165627a7a72305820835ef3817ef6936d1e1784981ee317fa358856250d94aa0e1d1b61f4235562f70029";

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
                        TransactionReceipt result = executeTransaction(function, fee);
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
                        TransactionReceipt result = executeTransaction(function, deposit);
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
                        TransactionReceipt result = executeTransaction(function, deposit);
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
