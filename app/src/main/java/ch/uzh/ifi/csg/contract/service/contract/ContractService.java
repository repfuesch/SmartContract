package ch.uzh.ifi.csg.contract.service.contract;


import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import ch.uzh.ifi.csg.contract.contract.IRentContract;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import ch.uzh.ifi.csg.contract.contract.TimeUnit;
import ch.uzh.ifi.csg.contract.datamodel.ContractInfo;

/**
 * Service interface to deploy/load contracts to/from the blockchain and to manage created contracts
 * on the local file system.
 */
public interface ContractService {

    /**
     * Deploys a purchase contract with the provided attributes on the blockchain and returns
     * an instance of {@link IPurchaseContract} that contains the logic to interact with the
     * smart contract.
     *
     * @param price
     * @param title
     * @param description
     * @param imageSignatures
     * @param verifyIdentity
     * @param lightDeployment
     * @return an instance of IPurchaseContract
     */
    SimplePromise<IPurchaseContract> deployPurchaseContract(
            BigInteger price,
            String title,
            String description,
            Map<String, String> imageSignatures,
            boolean verifyIdentity,
            boolean lightDeployment);

    /**
     * Deploys a rent contract with the provided attributes on the blockchain and returns
     * an instance of {@link IRentContract} that contains the logic to interact with the
     * smart contract.
     *
     * @param price
     * @param deposit
     * @param timeUnit
     * @param title
     * @param description
     * @param imageSignatures
     * @param verifyIdentity
     * @param lightDeployment
     * @return an instance of IRentContract
     */
    SimplePromise<IRentContract> deployRentContract(
            BigInteger price,
            BigInteger deposit,
            TimeUnit timeUnit,
            String title,
            String description,
            Map<String, String> imageSignatures,
            boolean verifyIdentity,
            boolean lightDeployment);

    /**
     * Creates an instance of {@link ITradeContract} that wraps the Ethereum contract referenced
     * by its contract address.
     *
     * @param contractType: specifies the concerete type of contract
     * @param contractAddress: the Ethereum address of the contract
     * @param accountId: the account to which the contract belongs
     * @return the loaded contract
     */
    SimplePromise<ITradeContract> loadContract(ContractType contractType, String contractAddress, String accountId);

    /**
     * Persists a contract on the local file system
     *
     * @param contract
     * @param account
     */
    void saveContract(ITradeContract contract, String account);

    /**
     * Removes a contract from the local file system
     *
     * @param contract
     * @param account
     */
    void removeContract(ITradeContract contract, String account);

    /**
     * Persists a contract on the local file system
     *
     * @param info
     * @param account
     */
    void saveContract(ContractInfo info, String account);

    /**
     * Removes a contract from the local file system
     *
     * @param contractAddress
     * @param account
     */
    void removeContract(String contractAddress, String account);

    /**
     * Returns a list of {@link ITradeContract} instances that wrap the smart contracts that belong
     * to an account
     *
     * @param accountId
     * @return
     */
    SimplePromise<List<ITradeContract>> loadContracts(String accountId);

    /**
     * Verifies if the smart contract stored at the given address contains the specified binary code.
     *
     * @param address
     * @param code
     * @return 'true' if address contains the code, 'false' otherwise
     */
    SimplePromise<Boolean> verifyContractCode(String address, String code);

}
