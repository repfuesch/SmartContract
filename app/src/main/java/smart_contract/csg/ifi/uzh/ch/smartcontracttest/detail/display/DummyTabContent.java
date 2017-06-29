package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail.display;

import android.content.Context;
import android.view.View;
import android.widget.TabHost;

/**
 * Created by flo on 26.06.17.
 */

public class DummyTabContent implements TabHost.TabContentFactory {
    private Context mContext;

    public DummyTabContent(Context context){
        mContext = context;
    }

    @Override
    public View createTabContent(String tag) {
        View v = new View(mContext);
        return v;
    }
}

