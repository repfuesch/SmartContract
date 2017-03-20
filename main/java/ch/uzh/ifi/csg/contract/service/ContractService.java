package ch.uzh.ifi.csg.contract.service;

import java.math.BigInteger;

import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;

/**
 * Created by flo on 24.02.17.
 */

public interface ContractService {

    SimplePromise<IPurchaseContract> deployContract(BigInteger value, String title, String description);
    SimplePromise<IPurchaseContract> loadContract(String contractAddress);
}
