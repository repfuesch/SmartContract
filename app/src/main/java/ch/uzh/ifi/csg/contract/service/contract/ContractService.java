package ch.uzh.ifi.csg.contract.service.contract;

import org.web3j.abi.datatypes.Bool;

import java.math.BigInteger;
import java.util.List;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;

/**
 * Created by flo on 24.02.17.
 */

public interface ContractService {

    SimplePromise<IPurchaseContract> deployContract(BigInteger value, String title, String description, boolean verifyIdentity);
    SimplePromise<IPurchaseContract> loadContract(String contractAddress, String account);
    void saveContract(IPurchaseContract contract, String account);
    void removeContract(IPurchaseContract contract, String account);
    SimplePromise<List<IPurchaseContract>> loadContracts(String account);
    SimplePromise<Boolean> isContract(String address);

}
