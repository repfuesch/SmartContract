package smart_contract.csg.ifi.uzh.ch.smartcontracttest.detail;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContractContactInfoFragment extends Fragment {


    public ContractContactInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contract_contact_info, container, false);
    }

}
