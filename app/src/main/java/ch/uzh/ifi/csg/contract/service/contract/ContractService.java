package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Bool;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;

/**
 * Created by flo on 24.02.17.
 */

public interface ContractService {

    SimplePromise<ITradeContract> deployPurchaseContract(
            BigInteger price,
            String title,
            String description,
            List<String> imageSignatures,
            boolean verifyIdentity);

    SimplePromise<ITradeContract> deployRentContract(
            BigInteger price,
            BigInteger deposit,
            TimeUnit timeUnit,
            String title,
            String description,
            List<String> imageSignatures,
            boolean verifyIdentity);

    SimplePromise<ITradeContract> loadContract(ContractType contractType, String contractAddress, String account);
    void saveContract(ITradeContract contract, String account);
    void removeContract(ITradeContract contract, String account);
    void saveContract(String contractAddress, ContractType contractType,  String account);
    void removeContract(String contractAddress, String account);
    SimplePromise<List<ITradeContract>> loadContracts(String account);
    SimplePromise<Boolean> isContract(String address);

}
