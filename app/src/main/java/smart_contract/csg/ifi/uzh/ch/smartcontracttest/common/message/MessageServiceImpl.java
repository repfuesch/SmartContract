package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.message;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;

import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityBase;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.ActivityChangedListener;
import smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.dialog.MessageDialogFragment;

/**
 * {@link MessageService} implementations that implements the {@link ActivityChangedListener} to
 * keep track of the active Activity. It displays the messages using a {@link MessageDialogFragment}
 */
public class MessageServiceImpl implements MessageService, ActivityChangedListener
{
    private ActivityBase activity;

    @Override
    public void showErrorMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.ERROR_MESSAGE, message);
        showMessageDialog(bundle);
    }

    @Override
    public void showMessage(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MessageDialogFragment.MESSAGE, message);
        showMessageDialog(bundle);
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

    private void showMessageDialog(final Bundle bundle) {

        if(activity == null)
            return;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogFragment errorDialogFragment = new MessageDialogFragment();
                errorDialogFragment.setArguments(bundle);
                errorDialogFragment.show(activity.getSupportFragmentManager(), "messageDialogFragment");
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
