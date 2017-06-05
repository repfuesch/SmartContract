package ch.uzh.ifi.csg.contract.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;
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
    public static final String BINARY = "0x606060405234620000005760405162000fa838038062000fa8833981016040528080518201919060200180518201919060200180519060200190919080518201919060200180519060200190919080519060200190919080519060200190919050505b8686868585885b816002819055508260038190555033600460006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508560009080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106200010657805160ff191683800117855562000137565b8280016001018555821562000137579182015b828111156200013657825182559160200191906001019062000119565b5b5090506200015f91905b808211156200015b57600081600090555060010162000141565b5090565b505083600560146101000a81548160ff0219169083151502179055508460019080519060200190828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10620001c957805160ff1916838001178555620001fa565b82800160010185558215620001fa579182015b82811115620001f9578251825591602001919060010190620001dc565b5b5090506200022291905b808211156200021e57600081600090555060010162000204565b5090565b50506000600760006101000a81548160ff021916908360038111620000005702179055506200026581620002a46401000000000262000b83176401000000009004565b5b50505050505080600181116200000057600760016101000a81548160ff021916908360018111620000005702179055505b505050505050506200034a565b6000600090505b815181101562000345576006805480600101828181548183558181151162000302578183600052602060002091820191016200030191905b80821115620002fd576000816000905550600101620002e3565b5090565b5b505050916000526020600020900160005b84848151811015620000005790602001906020020151909190915090600019169055505b8080600101915050620002ab565b5b5050565b610c4e806200035a6000396000f300606060405236156100d9576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308551a53146100de57806335a063b41461012d5780633c6a7f9b1461013c5780634a79d50c1461016a5780637150d8ae146102005780637284e4161461024f5780638500d668146102e55780638687b4dd146102ef5780639a078f2c146102fe578063a035b1fe14610370578063b06a428a14610393578063c0d047f0146103b6578063c19d93fb146103c0578063d0e30db0146103ee578063e8731c3214610411575b610000565b34610000576100eb610438565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761013a61045e565b005b346100005761014961053f565b6040518082600181116100005760ff16815260200191505060405180910390f35b3461000057610177610552565b60405180806020018281038252838181518152602001915080519060200190808383600083146101c6575b8051825260208311156101c6576020820191506020810190506020830392506101a2565b505050905090810190601f1680156101f25780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b346100005761020d6105f0565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b346100005761025c610616565b60405180806020018281038252838181518152602001915080519060200190808383600083146102ab575b8051825260208311156102ab57602082019150602081019050602083039250610287565b505050905090810190601f1680156102d75780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6102ed6106b4565b005b34610000576102fc610793565b005b346100005761030b6108ac565b604051808060200182810382528381815181526020019150805190602001906020028083836000831461035d575b80518252602083111561035d57602082019150602081019050602083039250610339565b5050509050019250505060405180910390f35b346100005761037d610918565b6040518082815260200191505060405180910390f35b34610000576103a061091e565b6040518082815260200191505060405180910390f35b6103be610977565b005b34610000576103cd610b57565b6040518082600381116100005760ff16815260200191505060405180910390f35b34610000576103fb610b6a565b6040518082815260200191505060405180910390f35b346100005761041e610b70565b604051808215151515815260200191505060405180910390f35b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156104ba57610000565b6000806003811161000057600760009054906101000a900460ff1660038111610000571415156104e957610000565b7f80b62b7017bb13cf105e22749ee2a06a417ffba8c7f57b665057e0f3c2e925d960405180905060405180910390a16002600760006101000a81548160ff0219169083600381116100005702179055505b5b505b565b600760019054906101000a900460ff1681565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105e85780601f106105bd576101008083540402835291602001916105e8565b820191906000526020600020905b8154815290600101906020018083116105cb57829003601f168201915b505050505081565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b60018054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106ac5780601f10610681576101008083540402835291602001916106ac565b820191906000526020600020905b81548152906001019060200180831161068f57829003601f168201915b505050505081565b6000806003811161000057600760009054906101000a900460ff1660038111610000571415156106e357610000565b60035434148015156106f457610000565b426009819055507f7643bb5e7abbcc632d9d551d022d8946a16be804d7552c78f68ee3730b4b1e6a60405180905060405180910390a133600560006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506001600760006101000a81548160ff0219169083600381116100005702179055505b5b505b50565b600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156107ef57610000565b60016003811161000057600760009054906101000a900460ff166003811161000057148061083c575060036003811161000057600760009054906101000a900460ff166003811161000057145b80151561084857610000565b61085061091e565b6008819055507f9910193657303db0842ab01c9d85764cedc6dff2431560f42e2372410b99932760405180905060405180910390a16003600760006101000a81548160ff0219169083600381116100005702179055505b5b505b565b6020604051908101604052806000815250600680548060200260200160405190810160405280929190818152602001828054801561090d57602002820191906000526020600020905b815460001916815260200190600101908083116108f5575b505050505090505b90565b60025481565b600060006000905060016001811161000057600760019054906101000a900460ff166001811161000057141561095f5762015180426009540302905061096b565b610e1042600954030290505b806002540291505b5090565b60006003806003811161000057600760009054906101000a900460ff1660038111610000571415156109a857610000565b600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff16141515610a0457610000565b600854341015801515610a1657610000565b7e827a5d9c6c82c525d526c3b31d84505a0c0ae33166031de8e10594dd2f329c60405180905060405180910390a16001600760006101000a81548160ff02191690836003811161000057021790555034600854039250600560009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc84600354019081150290604051809050600060405180830381858888f193505050501580610b445750600460009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc3073ffffffffffffffffffffffffffffffffffffffff16319081150290604051809050600060405180830381858888f19350505050155b15610b4e57610000565b5b5b505b5b5050565b600760009054906101000a900460ff1681565b60035481565b600560149054906101000a900460ff1681565b6000600090505b8151811015610c1d5760068054806001018281815481835581811511610bdc57818360005260206000209182019101610bdb91905b80821115610bd7576000816000905550600101610bbf565b5090565b5b505050916000526020600020900160005b848481518110156100005790602001906020020151909190915090600019169055505b8080600101915050610b8a565b5b50505600a165627a7a72305820ded1e7f5346dae6f781de6a4f4aba9bbb04a12ffec66b77e68bb5c25fe549f6d0029";

    protected RentContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    @Override
    public ContractType getContractType() {
        return ContractType.Rent;
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
        return Async.toPromise(
                new Callable<BigInteger>() {
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

    @Override
    public SimplePromise<TimeUnit> getTimeUnit() {
        return Async.toPromise(
                new Callable<TimeUnit>() {
                    @Override
                    public TimeUnit call() throws Exception {
                        Function function = new Function("timeUnit",
                                Arrays.<Type>asList(),
                                Arrays.<TypeReference<?>>asList(new TypeReference<Uint8>() {
                                }));
                        Uint8 result = executeCallSingleValueReturn(function);

                        return TimeUnit.valueOf(result.getValue().intValue());
                    }
                });
    }
}
