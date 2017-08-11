package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview.list;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.BusyIndicator;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message.MessageService;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContext;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of {@link ITradeContract} items.
 */
public class ContractListFragment extends Fragment
{
    private static final String ARG_COLUMN_COUNT = "column-count";

    private RecyclerView purchaseList;

    private int mColumnCount = 1;

    private LinearLayout contentView;
    private TradeContractRecyclerViewAdapter adapter;
    private ApplicationContext contextProvider;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContractListFragment() {
    }

    public Filter getListFilter()
    {
        return adapter.getFilter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(adapter != null)
            adapter.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchasecontract_list, container, false);

        contentView = (LinearLayout) view.findViewById(R.id.contract_list_content);

        // Set the adapter
        purchaseList = (RecyclerView) view.findViewById(R.id.purchase_list);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            purchaseList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            purchaseList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        adapter = new TradeContractRecyclerViewAdapter(contextProvider);
        purchaseList.setAdapter(adapter);
        registerForContextMenu(purchaseList);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    private void attachContext(Context context)
    {
        if (context instanceof ApplicationContextProvider) {
            contextProvider = ((ApplicationContextProvider) context).getAppContext();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ApplicationContextProvider");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachContext(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**Loads all contracts for an account and adds them to the list
     *
     * @param account
     * @return
     */
    public SimplePromise<List<ITradeContract>> loadContractsForAccount(String account)
    {
        BusyIndicator.show(contentView);
        return contextProvider.getServiceProvider().getContractService().loadContracts(account)
                .always(new AlwaysCallback<List<ITradeContract>>() {
                    @Override
                    public void onAlways(Promise.State state, final List<ITradeContract> resolved, final Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rejected != null)
                                {
                                    Log.e("overview", "Cannot load contracts", rejected);
                                }else{
                                    adapter.setContracts(resolved);
                                }

                                adapter.notifyDataSetChanged();
                                BusyIndicator.hide(contentView);
                            }
                        });
                    }
                });
    }

    /**
     * Loads a contract with the specified type and address and adds it to the list.
     *
     * @param type
     * @param contractAddress
     * @return
     */
    public SimplePromise<ITradeContract> loadContract(ContractType type, String contractAddress)
    {
        BusyIndicator.show(contentView);
        return contextProvider.getServiceProvider().getContractService().loadContract(type, contractAddress, contextProvider.getSettingProvider().getSelectedAccount())
                .always(new AlwaysCallback<ITradeContract>() {
                    @Override
                    public void onAlways(Promise.State state, final ITradeContract resolved, final Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rejected != null)
                                {
                                    Log.e("overview", "Cannot load contract", rejected);
                                }else{
                                    if(resolved != null)
                                    {
                                        adapter.addContract(resolved);
                                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                                    }
                                }

                                BusyIndicator.hide(contentView);
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        ITradeContract selectedContract = adapter.getSelectedContract();
        if(selectedContract == null)
            return super.onContextItemSelected(item);

        if(item.getTitle().equals("remove"))
        {
            //remove contract from the file system and reload list
            String selectedAccount = contextProvider.getSettingProvider().getSelectedAccount();
            contextProvider.getServiceProvider().getContractService().removeContract(selectedContract, selectedAccount);
            loadContractsForAccount(selectedAccount);
        }

        return super.onContextItemSelected(item);
    }

}
