package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.jdeferred.Promise;

import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.ContractType;
import ch.uzh.ifi.csg.contract.contract.ITradeContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.MessageHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.provider.ApplicationContextProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link MessageHandler}
 * interface.
 */
public class ContractListFragment extends Fragment
{
    private static final String ARG_COLUMN_COUNT = "column-count";

    private RecyclerView purchaseList;
    private LinearLayout progressView;

    private int mColumnCount = 1;

    private TradeContractRecyclerViewAdapter adapter;
    private List<ITradeContract> contracts;

    private MessageHandler errorHandler;
    private ApplicationContextProvider contextProvider;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContractListFragment() {
    }

    @SuppressWarnings("unused")
    public static ContractListFragment newInstance(int columnCount) {
        ContractListFragment fragment = new ContractListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        contracts = new ArrayList<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        adapter.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchasecontract_list, container, false);

        // Set the adapter
        purchaseList = (RecyclerView) view.findViewById(R.id.purchase_list);
        progressView = (LinearLayout) view.findViewById(R.id.purchase_list_progress_view);

        Context context = view.getContext();
        if (mColumnCount <= 1) {
            purchaseList.setLayoutManager(new LinearLayoutManager(context));
        } else {
            purchaseList.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }

        adapter = new TradeContractRecyclerViewAdapter(contracts, contextProvider);
        purchaseList.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachContext(context);
    }

    private void attachContext(Context context)
    {
        if (context instanceof MessageHandler) {
            errorHandler = (MessageHandler) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement MessageHandler");
        }

        if (context instanceof ApplicationContextProvider) {
            contextProvider = (ApplicationContextProvider) context;
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
        errorHandler = null;
    }

    public SimplePromise<List<ITradeContract>> loadContractsForAccount(String account)
    {
        contracts.clear();
        return contextProvider.getServiceProvider().getContractService().loadContracts(account)
                .always(new AlwaysCallback<List<ITradeContract>>() {
                    @Override
                    public void onAlways(Promise.State state, final List<ITradeContract> resolved, final Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rejected != null)
                                {
                                    errorHandler.handleError(rejected);
                                }else{
                                    contracts.addAll(resolved);
                                }

                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
    }

    public SimplePromise<ITradeContract> loadContract(ContractType type, String contractAddress)
    {
        return contextProvider.getServiceProvider().getContractService().loadContract(type, contractAddress, contextProvider.getSettingProvider().getSelectedAccount())
                .always(new AlwaysCallback<ITradeContract>() {
                    @Override
                    public void onAlways(Promise.State state, final ITradeContract resolved, final Throwable rejected) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(rejected != null)
                                {
                                    errorHandler.handleError(rejected);
                                }else{
                                    contracts.add(resolved);
                                    adapter.notifyItemInserted(contracts.size() - 1);
                                }
                            }
                        });
                    }
                });
    }

}
