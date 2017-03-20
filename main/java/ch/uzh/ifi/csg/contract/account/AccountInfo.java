package ch.uzh.ifi.csg.contract.account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flo on 06.03.17.
 */

public class AccountInfo
{
    private String AccountId;
    private List<ContractInfo> contractInfo;

    public AccountInfo(String accountId) {
        AccountId = accountId;
        contractInfo = new ArrayList<>();
    }

    public String getAccountId() {
        return AccountId;
    }

    public void setAccountId(String accountId) {
        AccountId = accountId;
    }

    public List<ContractInfo> getContractInfo() {
        return contractInfo;
    }

    public void setContractInfo(List<ContractInfo> contractInfo) {
        this.contractInfo = contractInfo;
    }

}
