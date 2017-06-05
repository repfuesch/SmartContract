package ch.uzh.ifi.csg.contract.contract;

import java.util.HashMap;
import java.util.Map;

public enum ContractState {
	Created(0), Locked(1), Inactive(2), AwaitPayment(3);

    private final int state;

    private final static Map<Integer, ContractState> map = initMap();

    private ContractState(final int state_num) {
        state = state_num;
    }

    private static Map<Integer, ContractState> initMap()
    {
        Map<Integer, ContractState> hmap = new HashMap<>();
        for(ContractState state : ContractState.values())
        {
            hmap.put(state.ordinal(), state);
        }
        return hmap;
    }

    public static ContractState valueOf(int state) {
        return map.get(state);
    }
}
