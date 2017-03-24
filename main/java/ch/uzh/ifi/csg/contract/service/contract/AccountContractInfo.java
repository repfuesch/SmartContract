package ch.uzh.ifi.csg.contract.service.contract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flo on 06.03.17.
 */

public class AccountContractInfo
{
    private String accountId;
    private List<ContractInfo> contractInfo;

    public AccountContractInfo(String accountId) {
        this.accountId = accountId;
        contractInfo = new ArrayList<>();
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<ContractInfo> getContractInfo() {
        return contractInfo;
    }

    public void setContractInfo(List<ContractInfo> contractInfo) {
        this.contractInfo = contractInfo;
    }

}
