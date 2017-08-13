package ch.uzh.ifi.csg.smartcontract.library.contract;

import java.util.HashMap;
import java.util.Map;

/**
 *Enum that stores the TimeUnits that can be used in a rent contract
 */
public enum TimeUnit {
    Hours(0), Days(1);

    private final int timeUnit;

    private final static Map<Integer, TimeUnit> map = initMap();

    private TimeUnit(final int state_num) {
        timeUnit = state_num;
    }

    private static Map<Integer, TimeUnit> initMap()
    {
        Map<Integer, TimeUnit> hmap = new HashMap<>();
        for(TimeUnit unit : TimeUnit.values())
        {
            hmap.put(unit.ordinal(), unit);
        }
        return hmap;
    }

    public static TimeUnit valueOf(int unit) {
        return map.get(unit);
    }
}
