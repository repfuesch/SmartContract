package smart_contract.csg.ifi.uzh.ch.smartcontracttest.common.validation;

import android.view.View;
import android.widget.TextView;

/**
 * TextFieldValidator to validate required fields
 */

public class RequiredTextFieldValidator extends TextFieldValidator implements View.OnFocusChangeListener
{
    public RequiredTextFieldValidator(TextView textView) {

        super(textView);
        textView.setError(textView.getHint() + " is required");
    }

    @Override
    public void validate(TextView textView) {
        if(textView.getText().toString().isEmpty())
        {
            textView.setError(textView.getHint() + " is required");
            setValid(false);
            return;
        }

        textView.setError(null);
        setValid(true);
    }

    @Override
    public void onFocusChange(View view, boolean b) {

        if(!view.hasFocus())
        {
            validate((TextView)view);
        }
    }
}
