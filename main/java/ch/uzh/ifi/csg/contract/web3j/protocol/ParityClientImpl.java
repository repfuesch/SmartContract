package ch.uzh.ifi.csg.contract.web3j.protocol;

import org.web3j.crypto.WalletFile;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.ShhFilter;
import org.web3j.protocol.core.methods.request.ShhPost;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.DbGetHex;
import org.web3j.protocol.core.methods.response.DbGetString;
import org.web3j.protocol.core.methods.response.DbPutHex;
import org.web3j.protocol.core.methods.response.DbPutString;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthCoinbase;
import org.web3j.protocol.core.methods.response.EthCompileLLL;
import org.web3j.protocol.core.methods.response.EthCompileSerpent;
import org.web3j.protocol.core.methods.response.EthCompileSolidity;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthFilter;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetBlockTransactionCountByHash;
import org.web3j.protocol.core.methods.response.EthGetBlockTransactionCountByNumber;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetCompilers;
import org.web3j.protocol.core.methods.response.EthGetStorageAt;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthGetUncleCountByBlockHash;
import org.web3j.protocol.core.methods.response.EthGetUncleCountByBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetWork;
import org.web3j.protocol.core.methods.response.EthHashrate;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthMining;
import org.web3j.protocol.core.methods.response.EthProtocolVersion;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthSign;
import org.web3j.protocol.core.methods.response.EthSubmitHashrate;
import org.web3j.protocol.core.methods.response.EthSubmitWork;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.EthUninstallFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.NetListening;
import org.web3j.protocol.core.methods.response.NetPeerCount;
import org.web3j.protocol.core.methods.response.NetVersion;
import org.web3j.protocol.core.methods.response.ShhAddToGroup;
import org.web3j.protocol.core.methods.response.ShhHasIdentity;
import org.web3j.protocol.core.methods.response.ShhMessages;
import org.web3j.protocol.core.methods.response.ShhNewFilter;
import org.web3j.protocol.core.methods.response.ShhNewGroup;
import org.web3j.protocol.core.methods.response.ShhNewIdentity;
import org.web3j.protocol.core.methods.response.ShhUninstallFilter;
import org.web3j.protocol.core.methods.response.ShhVersion;
import org.web3j.protocol.core.methods.response.VoidResponse;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.core.methods.response.Web3Sha3;
import org.web3j.protocol.parity.Parity;
import org.web3j.protocol.parity.methods.response.NewAccountIdentifier;
import org.web3j.protocol.parity.methods.response.PersonalAccountsInfo;
import org.web3j.protocol.parity.methods.response.PersonalListAccounts;
import org.web3j.protocol.parity.methods.response.PersonalRejectRequest;
import org.web3j.protocol.parity.methods.response.PersonalRequestsToConfirm;
import org.web3j.protocol.parity.methods.response.PersonalSignerEnabled;
import org.web3j.protocol.parity.methods.response.PersonalUnlockAccount;
import org.web3j.utils.Numeric;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import ch.uzh.ifi.csg.contract.web3j.protocol.rx.JsonRpcRxClient;
import rx.Observable;

/**
 * JSON-RPC 2.0 factory implementation for Parity.
 * This code is copied from JsonRpc2_0Web3j
 */
public class ParityClientImpl implements Parity {

    protected static final long ID = 1;
    static final int BLOCK_TIME = 15 * 1000;

    protected final Web3jService web3jService;
    private final JsonRpcRxClient web3jRx;
    private final long blockTime;

    public ParityClientImpl(Web3jService web3jService) {

        this(web3jService, BLOCK_TIME, Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public ParityClientImpl(Web3jService web3jService, long pollingInterval, ScheduledExecutorService executorService)
    {
        this.web3jService = web3jService;
        this.web3jRx = new JsonRpcRxClient(this, executorService);
        this.blockTime = pollingInterval;
    }

    @Override
    public Request<?, Web3ClientVersion> web3ClientVersion() {
        return new Request<String, Web3ClientVersion>(
                "web3_clientVersion",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                Web3ClientVersion.class);
    }

    @Override
    public Request<?, Web3Sha3> web3Sha3(String data) {
        return new Request<String, Web3Sha3>(
                "web3_sha3",
                Arrays.asList(data),
                ID,
                web3jService,
                Web3Sha3.class);
    }

    @Override
    public Request<?, NetVersion> netVersion() {
        return new Request<String, NetVersion>(
                "net_version",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                NetVersion.class);
    }

    @Override
    public Request<?, NetListening> netListening() {
        return new Request<String, NetListening>(
                "net_listening",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                NetListening.class);
    }

    @Override
    public Request<?, NetPeerCount> netPeerCount() {
        return new Request<String, NetPeerCount>(
                "net_peerCount",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                NetPeerCount.class);
    }

    @Override
    public Request<?, EthProtocolVersion> ethProtocolVersion() {
        return new Request<String, EthProtocolVersion>(
                "eth_protocolVersion",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthProtocolVersion.class);
    }

    @Override
    public Request<?, EthCoinbase> ethCoinbase() {
        return new Request<String, EthCoinbase>(
                "eth_coinbase",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthCoinbase.class);
    }

    @Override
    public Request<?, EthSyncing> ethSyncing() {
        return new Request<String, EthSyncing>(
                "eth_syncing",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthSyncing.class);
    }

    @Override
    public Request<?, EthMining> ethMining() {
        return new Request<String, EthMining>(
                "eth_mining",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthMining.class);
    }

    @Override
    public Request<?, EthHashrate> ethHashrate() {
        return new Request<String, EthHashrate>(
                "eth_hashrate",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthHashrate.class);
    }

    @Override
    public Request<?, EthGasPrice> ethGasPrice() {
        return new Request<String, EthGasPrice>(
                "eth_gasPrice",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthGasPrice.class);
    }

    @Override
    public Request<?, EthAccounts> ethAccounts() {
        return new Request<String, EthAccounts>(
                "eth_accounts",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthAccounts.class);
    }

    @Override
    public Request<?, EthBlockNumber> ethBlockNumber() {
        return new Request<String, EthBlockNumber>(
                "eth_blockNumber",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthBlockNumber.class);
    }

    @Override
    public Request<?, EthGetBalance> ethGetBalance(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetBalance>(
                "eth_getBalance",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetBalance.class);
    }

    @Override
    public Request<?, EthGetStorageAt> ethGetStorageAt(
            String address, BigInteger position, DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetStorageAt>(
                "eth_getStorageAt",
                Arrays.asList(
                        address,
                        Numeric.encodeQuantity(position),
                        defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetStorageAt.class);
    }

    @Override
    public Request<?, EthGetTransactionCount> ethGetTransactionCount(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetTransactionCount>(
                "eth_getTransactionCount",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetTransactionCount.class);
    }

    @Override
    public Request<?, EthGetBlockTransactionCountByHash> ethGetBlockTransactionCountByHash(
            String blockHash) {
        return new Request<String, EthGetBlockTransactionCountByHash>(
                "eth_getBlockTransactionCountByHash",
                Arrays.asList(blockHash),
                ID,
                web3jService,
                EthGetBlockTransactionCountByHash.class);
    }

    @Override
    public Request<?, EthGetBlockTransactionCountByNumber> ethGetBlockTransactionCountByNumber(
            DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetBlockTransactionCountByNumber>(
                "eth_getBlockTransactionCountByNumber",
                Arrays.asList(defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetBlockTransactionCountByNumber.class);
    }

    @Override
    public Request<?, EthGetUncleCountByBlockHash> ethGetUncleCountByBlockHash(String blockHash) {
        return new Request<String, EthGetUncleCountByBlockHash>(
                "eth_getUncleCountByBlockHash",
                Arrays.asList(blockHash),
                ID,
                web3jService,
                EthGetUncleCountByBlockHash.class);
    }

    @Override
    public Request<?, EthGetUncleCountByBlockNumber> ethGetUncleCountByBlockNumber(
            DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetUncleCountByBlockNumber>(
                "eth_getUncleCountByBlockNumber",
                Arrays.asList(defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetUncleCountByBlockNumber.class);
    }

    @Override
    public Request<?, EthGetCode> ethGetCode(
            String address, DefaultBlockParameter defaultBlockParameter) {
        return new Request<String, EthGetCode>(
                "eth_getCode",
                Arrays.asList(address, defaultBlockParameter.getValue()),
                ID,
                web3jService,
                EthGetCode.class);
    }

    @Override
    public Request<?, EthSign> ethSign(String address, String sha3HashOfDataToSign) {
        return new Request<String, EthSign>(
                "eth_sign",
                Arrays.asList(address, sha3HashOfDataToSign),
                ID,
                web3jService,
                EthSign.class);
    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.EthSendTransaction> ethSendTransaction(
            Transaction transaction) {
        return new Request<Transaction, EthSendTransaction>(
                "eth_sendTransaction",
                Arrays.asList(transaction),
                ID,
                web3jService,
                org.web3j.protocol.core.methods.response.EthSendTransaction.class);
    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.EthSendTransaction> ethSendRawTransaction(
            String signedTransactionData) {
        return new Request<String, EthSendTransaction>(
                "eth_sendRawTransaction",
                Arrays.asList(signedTransactionData),
                ID,
                web3jService,
                org.web3j.protocol.core.methods.response.EthSendTransaction.class);
    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.EthCall> ethCall(
            Transaction transaction, DefaultBlockParameter defaultBlockParameter) {
        return new Request<Object, EthCall>(
                "eth_call",
                Arrays.asList(transaction, defaultBlockParameter),
                ID,
                web3jService,
                org.web3j.protocol.core.methods.response.EthCall.class);
    }

    @Override
    public Request<?, EthEstimateGas> ethEstimateGas(Transaction transaction) {
        return new Request<Transaction, EthEstimateGas>(
                "eth_estimateGas",
                Arrays.asList(transaction),
                ID,
                web3jService,
                EthEstimateGas.class);
    }

    @Override
    public Request<?, EthBlock> ethGetBlockByHash(
            String blockHash, boolean returnFullTransactionObjects) {
        return new Request<Object, EthBlock>(
                "eth_getBlockByHash",
                Arrays.<Object>asList(
                        blockHash,
                        returnFullTransactionObjects),
                ID,
                web3jService,
                EthBlock.class);
    }

    @Override
    public Request<?, EthBlock> ethGetBlockByNumber(
            DefaultBlockParameter defaultBlockParameter,
            boolean returnFullTransactionObjects) {
        return new Request<Object, EthBlock>(
                "eth_getBlockByNumber",
                Arrays.<Object>asList(
                        defaultBlockParameter.getValue(),
                        returnFullTransactionObjects),
                ID,
                web3jService,
                EthBlock.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByHash(String transactionHash) {
        return new Request<String, EthTransaction>(
                "eth_getTransactionByHash",
                Arrays.asList(transactionHash),
                ID,
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return new Request<String, EthTransaction>(
                "eth_getTransactionByBlockHashAndIndex",
                Arrays.asList(
                        blockHash,
                        Numeric.encodeQuantity(transactionIndex)),
                ID,
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthTransaction> ethGetTransactionByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger transactionIndex) {
        return new Request<String, EthTransaction>(
                "eth_getTransactionByBlockNumberAndIndex",
                Arrays.asList(
                        defaultBlockParameter.getValue(),
                        Numeric.encodeQuantity(transactionIndex)),
                ID,
                web3jService,
                EthTransaction.class);
    }

    @Override
    public Request<?, EthGetTransactionReceipt> ethGetTransactionReceipt(String transactionHash) {
        return new Request<String, EthGetTransactionReceipt>(
                "eth_getTransactionReceipt",
                Arrays.asList(transactionHash),
                ID,
                web3jService,
                EthGetTransactionReceipt.class);
    }

    @Override
    public Request<?, EthBlock> ethGetUncleByBlockHashAndIndex(
            String blockHash, BigInteger transactionIndex) {
        return new Request<String, EthBlock>(
                "eth_getUncleByBlockHashAndIndex",
                Arrays.asList(
                        blockHash,
                        Numeric.encodeQuantity(transactionIndex)),
                ID,
                web3jService,
                EthBlock.class);
    }

    @Override
    public Request<?, EthBlock> ethGetUncleByBlockNumberAndIndex(
            DefaultBlockParameter defaultBlockParameter, BigInteger uncleIndex) {
        return new Request<String, EthBlock>(
                "eth_getUncleByBlockNumberAndIndex",
                Arrays.asList(
                        defaultBlockParameter.getValue(),
                        Numeric.encodeQuantity(uncleIndex)),
                ID,
                web3jService,
                EthBlock.class);
    }

    @Override
    public Request<?, EthGetCompilers> ethGetCompilers() {
        return new Request<String, EthGetCompilers>(
                "eth_getCompilers",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthGetCompilers.class);
    }

    @Override
    public Request<?, EthCompileLLL> ethCompileLLL(String sourceCode) {
        return new Request<String, EthCompileLLL>(
                "eth_compileLLL",
                Arrays.asList(sourceCode),
                ID,
                web3jService,
                EthCompileLLL.class);
    }

    @Override
    public Request<?, EthCompileSolidity> ethCompileSolidity(String sourceCode) {
        return new Request<String, EthCompileSolidity>(
                "eth_compileSolidity",
                Arrays.asList(sourceCode),
                ID,
                web3jService,
                EthCompileSolidity.class);
    }

    @Override
    public Request<?, EthCompileSerpent> ethCompileSerpent(String sourceCode) {
        return new Request<String, EthCompileSerpent>(
                "eth_compileSerpent",
                Arrays.asList(sourceCode),
                ID,
                web3jService,
                EthCompileSerpent.class);
    }

    @Override
    public Request<?, EthFilter> ethNewFilter(
            org.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        return new Request<org.web3j.protocol.core.methods.request.EthFilter, EthFilter>(
                "eth_newFilter",
                Arrays.asList(ethFilter),
                ID,
                web3jService,
                EthFilter.class);
    }

    @Override
    public Request<?, EthFilter> ethNewBlockFilter() {
        return new Request<String, EthFilter>(
                "eth_newBlockFilter",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthFilter.class);
    }

    @Override
    public Request<?, EthFilter> ethNewPendingTransactionFilter() {
        return new Request<String, EthFilter>(
                "eth_newPendingTransactionFilter",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthFilter.class);
    }

    @Override
    public Request<?, EthUninstallFilter> ethUninstallFilter(BigInteger filterId) {
        return new Request<String, EthUninstallFilter>(
                "eth_uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                EthUninstallFilter.class);
    }

    @Override
    public Request<?, EthLog> ethGetFilterChanges(BigInteger filterId) {
        return new Request<String, EthLog>(
                "eth_getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                EthLog.class);
    }

    @Override
    public Request<?, EthLog> ethGetFilterLogs(BigInteger filterId) {
        return new Request<String, EthLog>(
                "eth_getFilterLogs",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                EthLog.class);
    }

    @Override
    public Request<?, EthLog> ethGetLogs(org.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        return new Request<org.web3j.protocol.core.methods.request.EthFilter, EthLog>(
                "eth_getLogs",
                Arrays.asList(ethFilter),
                ID,
                web3jService,
                EthLog.class);
    }

    @Override
    public Request<?, EthGetWork> ethGetWork() {
        return new Request<String, EthGetWork>(
                "eth_getWork",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                EthGetWork.class);
    }

    @Override
    public Request<?, EthSubmitWork> ethSubmitWork(String nonce, String headerPowHash, String mixDigest) {
        return new Request<String, EthSubmitWork>(
                "eth_submitWork",
                Arrays.asList(nonce, headerPowHash, mixDigest),
                ID,
                web3jService,
                EthSubmitWork.class);
    }

    @Override
    public Request<?, EthSubmitHashrate> ethSubmitHashrate(String hashrate, String clientId) {
        return new Request<String, EthSubmitHashrate>(
                "eth_submitHashrate",
                Arrays.asList(hashrate, clientId),
                ID,
                web3jService,
                EthSubmitHashrate.class);
    }

    @Override
    public Request<?, DbPutString> dbPutString(
            String databaseName, String keyName, String stringToStore) {
        return new Request<String, DbPutString>(
                "db_putString",
                Arrays.asList(databaseName, keyName, stringToStore),
                ID,
                web3jService,
                DbPutString.class);
    }

    @Override
    public Request<?, DbGetString> dbGetString(String databaseName, String keyName) {
        return new Request<String, DbGetString>(
                "db_getString",
                Arrays.asList(databaseName, keyName),
                ID,
                web3jService,
                DbGetString.class);
    }

    @Override
    public Request<?, DbPutHex> dbPutHex(String databaseName, String keyName, String dataToStore) {
        return new Request<String, DbPutHex>(
                "db_putHex",
                Arrays.asList(databaseName, keyName, dataToStore),
                ID,
                web3jService,
                DbPutHex.class);
    }

    @Override
    public Request<?, DbGetHex> dbGetHex(String databaseName, String keyName) {
        return new Request<String, DbGetHex>(
                "db_getHex",
                Arrays.asList(databaseName, keyName),
                ID,
                web3jService,
                DbGetHex.class);
    }

    @Override
    public Request<?, org.web3j.protocol.core.methods.response.ShhPost> shhPost(ShhPost shhPost) {
        return new Request<ShhPost, org.web3j.protocol.core.methods.response.ShhPost>(
                "shh_post",
                Arrays.asList(shhPost),
                ID,
                web3jService,
                org.web3j.protocol.core.methods.response.ShhPost.class);
    }

    @Override
    public Request<?, ShhVersion> shhVersion() {
        return new Request<String, ShhVersion>(
                "shh_version",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                ShhVersion.class);
    }

    @Override
    public Request<?, ShhNewIdentity> shhNewIdentity() {
        return new Request<String, ShhNewIdentity>(
                "shh_newIdentity",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                ShhNewIdentity.class);
    }

    @Override
    public Request<?, ShhHasIdentity> shhHasIdentity(String identityAddress) {
        return new Request<String, ShhHasIdentity>(
                "shh_hasIdentity",
                Arrays.asList(identityAddress),
                ID,
                web3jService,
                ShhHasIdentity.class);
    }

    @Override
    public Request<?, ShhNewGroup> shhNewGroup() {
        return new Request<String, ShhNewGroup>(
                "shh_newGroup",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                ShhNewGroup.class);
    }

    @Override
    public Request<?, ShhAddToGroup> shhAddToGroup(String identityAddress) {
        return new Request<String, ShhAddToGroup>(
                "shh_addToGroup",
                Arrays.asList(identityAddress),
                ID,
                web3jService,
                ShhAddToGroup.class);
    }

    @Override
    public Request<?, ShhNewFilter> shhNewFilter(ShhFilter shhFilter) {
        return new Request<ShhFilter, ShhNewFilter>(
                "shh_newFilter",
                Arrays.asList(shhFilter),
                ID,
                web3jService,
                ShhNewFilter.class);
    }

    @Override
    public Request<?, ShhUninstallFilter> shhUninstallFilter(BigInteger filterId) {
        return new Request<String, ShhUninstallFilter>(
                "shh_uninstallFilter",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                ShhUninstallFilter.class);
    }

    @Override
    public Request<?, ShhMessages> shhGetFilterChanges(BigInteger filterId) {
        return new Request<String, ShhMessages>(
                "shh_getFilterChanges",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                ShhMessages.class);
    }

    @Override
    public Request<?, ShhMessages> shhGetMessages(BigInteger filterId) {
        return new Request<String, ShhMessages>(
                "shh_getMessages",
                Arrays.asList(Numeric.encodeQuantity(filterId)),
                ID,
                web3jService,
                ShhMessages.class);
    }

    @Override
    public Observable<String> ethBlockHashObservable() {
        return web3jRx.ethBlockHashObservable(blockTime);
    }

    @Override
    public Observable<String> ethPendingTransactionHashObservable() {
        return web3jRx.ethPendingTransactionHashObservable(blockTime);
    }

    @Override
    public Observable<Log> ethLogObservable(
            org.web3j.protocol.core.methods.request.EthFilter ethFilter) {
        return web3jRx.ethLogObservable(ethFilter, blockTime);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
    transactionObservable() {
        return web3jRx.transactionObservable(blockTime);
    }

    @Override
    public Observable<org.web3j.protocol.core.methods.response.Transaction>
    pendingTransactionObservable() {
        return web3jRx.pendingTransactionObservable(blockTime);
    }

    @Override
    public Observable<EthBlock> blockObservable(boolean fullTransactionObjects) {
        return web3jRx.blockObservable(fullTransactionObjects, blockTime);
    }
    @Override
    public Request<?, PersonalSignerEnabled> personalSignerEnabled() {
        return new Request<String, PersonalSignerEnabled>(
                "personal_signerEnabled",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                PersonalSignerEnabled.class);
    }

    @Override
    public Request<?, PersonalListAccounts> personalListAccounts() {
        return new Request<String, PersonalListAccounts>(
                "personal_listAccounts",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                PersonalListAccounts.class);
    }

    @Override
    public Request<?, NewAccountIdentifier> personalNewAccount(String password) {
        return new Request<String, NewAccountIdentifier>(
                "personal_newAccount",
                Arrays.asList(password),
                ID,
                web3jService,
                NewAccountIdentifier.class);
    }

    @Override
    public Request<?, NewAccountIdentifier> personalNewAccountFromPhrase(String phrase, String password) {
        return new Request<String, NewAccountIdentifier>(
                "personal_newAccountFromPhrase",
                Arrays.asList(phrase, password),
                ID,
                web3jService,
                NewAccountIdentifier.class);
    }

    @Override
    public Request<?, NewAccountIdentifier> personalNewAccountFromWallet(WalletFile walletFile, String password) {
        return new Request<Object, NewAccountIdentifier>(
                "personal_newAccountFromWallet",
                Arrays.asList(walletFile, password),
                ID,
                web3jService,
                NewAccountIdentifier.class);
    }

    @Override
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(
            String accountId, String password, BigInteger duration) {
        List<Object> attributes = new ArrayList<Object>(3);
        attributes.add(accountId);
        attributes.add(password);

        if (duration != null) {
            // Parity has a bug where it won't support a duration
            // See https://github.com/ethcore/parity/issues/1215
            attributes.add(duration.longValue());
        } else {
            attributes.add(null);  // we still need to include the null value, otherwise Parity rejects
        }

        return new Request<Object, PersonalUnlockAccount>(
                "personal_unlockAccount",
                attributes,
                ID,
                web3jService,
                PersonalUnlockAccount.class);
    }

    @Override
    public Request<?, PersonalUnlockAccount> personalUnlockAccount(String accountId, String password) {
        return personalUnlockAccount(accountId, password, null);
    }

    @Override
    public Request<?, EthSendTransaction> personalSignAndSendTransaction(
            Transaction transaction, String password) {
        return new Request<Object, EthSendTransaction>(
                "personal_signAndSendTransaction",
                Arrays.asList(transaction, password),
                ID,
                web3jService,
                EthSendTransaction.class);
    }

    @Override
    public Request<?, VoidResponse> personalSetAccountName(String accountId, String newAccountName) {
        return new Request<String, VoidResponse>(
                "personal_setAccountName",
                Arrays.asList(accountId, newAccountName),
                ID,
                web3jService,
                VoidResponse.class);
    }

    @Override
    public Request<?, VoidResponse> personalSetAccountMeta(
            String accountId, Map<String, Object> metadata) {
        return new Request<Object, VoidResponse>(
                "personal_setAccountMeta",
                Arrays.asList(accountId, metadata),
                ID,
                web3jService,
                VoidResponse.class);
    }

    @Override
    public Request<?, PersonalAccountsInfo> personalAccountsInfo() {
        return new Request<String, PersonalAccountsInfo>(
                "personal_accountsInfo",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                PersonalAccountsInfo.class);
    }

    @Override
    public Request<?, PersonalRequestsToConfirm> personalRequestsToConfirm() {
        return new Request<String, PersonalRequestsToConfirm>(
                "personal_requestsToConfirm",
                Collections.<String>emptyList(),
                ID,
                web3jService,
                PersonalRequestsToConfirm.class);
    }

    @Override
    public Request<?, EthSendTransaction> personalConfirmRequest(String requestId, Transaction transaction, String password) {
        return new Request<Object, EthSendTransaction>(
                "personal_confirmRequest",
                Arrays.asList(requestId, transaction, password),
                ID,
                web3jService,
                EthSendTransaction.class);
    }

    @Override
    public Request<?, PersonalRejectRequest> personalRejectRequest(String requestId) {
        return new Request<String, PersonalRejectRequest>(
                "personal_rejectRequest",
                Arrays.asList(requestId),
                ID,
                web3jService,
                PersonalRejectRequest.class);
    }
}
