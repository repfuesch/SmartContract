package smart_contract.csg.ifi.uzh.ch.smartcontracttest.overview;

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

import ch.uzh.ifi.csg.contract.account.ContractInfo;
import ch.uzh.ifi.csg.contract.account.ContractManager;
import ch.uzh.ifi.csg.contract.account.ContractFileManager;
import ch.uzh.ifi.csg.contract.async.promise.AlwaysCallback;
import ch.uzh.ifi.csg.contract.async.promise.DoneCallback;
import ch.uzh.ifi.csg.contract.async.promise.FailCallback;
import ch.uzh.ifi.csg.contract.async.promise.SimplePromise;
import ch.uzh.ifi.csg.contract.contract.IPurchaseContract;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ContractErrorHandler;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.R;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ServiceProvider;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link ContractErrorHandler}
 * interface.
 */
public class PurchaseContractFragment extends Fragment
{
    private static final String ARG_COLUMN_COUNT = "column-count";

    private RecyclerView purchaseList;
    private LinearLayout progressView;

    private int mColumnCount = 1;
    private ContractErrorHandler errorHandler;
    private PurchaseContractRecyclerViewAdapter adapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PurchaseContractFragment() {
    }

    @SuppressWarnings("unused")
    public static PurchaseContractFragment newInstance(int columnCount) {
        PurchaseContractFragment fragment = new PurchaseContractFragment();
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

        adapter = new PurchaseContractRecyclerViewAdapter(errorHandler);
        purchaseList.setAdapter(adapter);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ContractErrorHandler) {
            errorHandler = (ContractErrorHandler) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ContractErrorHandler");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        errorHandler = null;
    }

    public void loadContractsForAccount(String account)
    {
        ContractManager contractManager = new ContractFileManager(getContext().getFilesDir() + "/accounts");
        List<ContractInfo> contractInfos = contractManager.loadContracts(account);
        adapter.clearContracts();

        for(final ContractInfo info : contractInfos)
        {
            ServiceProvider.getInstance().getContractService().loadContract(info.getContractAddress())
                    .always(new AlwaysCallback<IPurchaseContract>() {
                        @Override
                        public void onAlways(Promise.State state, final IPurchaseContract resolved, final Throwable rejected) {
                            if(rejected != null)
                            {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        errorHandler.handleError(rejected);
                                    }
                                });

                            }else{
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!info.getLastState().equals(resolved.state().get()))
                                        {
                                            //todo: state changed since last login.
                                        }

                                        adapter.addContract(resolved);
                                    }
                                });
                            }
                        }
                    });
        }
    }

    public void loadContract(IPurchaseContract newContract)
    {
        adapter.addContract(newContract);
    }

    private void loadContract(SimplePromise<IPurchaseContract> promise)
    {
        promise
                .done(new DoneCallback<IPurchaseContract>() {
                    @Override
                    public void onDone(final IPurchaseContract result)
                    {
                        //add contract to list and persist it
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadContract(result);
                            }
                        });
                    }
                })
                .fail(new FailCallback() {
                    @Override
                    public void onFail(final Throwable rejected) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                errorHandler.handleError(rejected);
                            }
                        });
                    }
                });
    }

    public void loadContract(String contractAddress)
    {
        SimplePromise<IPurchaseContract> promise = ServiceProvider.getInstance().getContractService().loadContract(contractAddress);
        loadContract(promise);
    }

    public void updateContract(String contractAddress)
    {
        adapter.updateContract(contractAddress);
    }

}
