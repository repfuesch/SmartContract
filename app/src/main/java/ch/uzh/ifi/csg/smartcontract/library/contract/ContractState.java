package ch.uzh.ifi.csg.smartcontract.library.contract;

import java.util.HashMap;
import java.util.Map;

/*
 * Enum that contains all states that a contract implementation can be in. A contract does
 * not necessarily account for all declared states.
 */
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

    /**
     * Method that returns the ContractState value for a given integer
     *
     * @param state
     * @return a ContractState or a KeyNotFoundException in case the given state does not exist
     */
    public static ContractState valueOf(int state) {
        return map.get(state);
    }
}
