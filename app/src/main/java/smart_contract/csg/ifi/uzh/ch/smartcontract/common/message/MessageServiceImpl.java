package smart_contract.csg.ifi.uzh.ch.smartcontract.common.message;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.ActivityChangedListener;
import smart_contract.csg.ifi.uzh.ch.smartcontract.common.dialog.MessageDialogFragment;

/**
 * {@link MessageService} implementations that implements the {@link ActivityChangedListener} to
 * keep track of the active Activity. It displays the messages using a {@link MessageDialogFragment}
 */
public class MessageServiceImpl implements MessageService, ActivityChangedListener
{
    private ActivityBase activity;
    private List<MessageDialogFragment> dialogs;

    public MessageServiceImpl()
    {
        dialogs = new ArrayList<>();
    }

    @Override
    public void showErrorMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.ERROR_MESSAGE, message);
        showMessageDialog(bundle, message);
    }

    @Override
    public void showMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.MESSAGE, message);
        showMessageDialog(bundle, message);
    }

    @Override
    public void showSnackBarMessage(final String message, final int length) {
        if(activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(activity.findViewById(android.R.id.content), message,
                        length)
                        .show();
            }
        });
    }

    private void showMessageDialog(final Bundle bundle, final String message) {

        if(activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //ensure that a specific message can not be displayed more than once
                FragmentManager fm = activity.getSupportFragmentManager();
                for(MessageDialogFragment dialog : dialogs)
                {
                    if(dialog.getMessage().equals(message))
                        return;
                }

                //Create and display the DialogFragment
                final MessageDialogFragment messageDialogFragment = new MessageDialogFragment();
                messageDialogFragment.setArguments(bundle);
                messageDialogFragment.show(fm, "messageDialogFragment");

                dialogs.add(messageDialogFragment);
                messageDialogFragment.setDialogListener(new MessageDialogFragment.MessageDialogListener() {
                    @Override
                    public void onDialogClosed() {
                        dialogs.remove(messageDialogFragment);
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResumed(ActivityBase activity) {
        this.activity = activity;
    }

    @Override
    public void onActivityStopped(ActivityBase activity) {
        this.activity = null;
    }
}
