package smart_contract.csg.ifi.uzh.ch.smartcontract.common.validation;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * Validator base class for validation of {@link TextView} controls
 */
public abstract class TextFieldValidator implements TextWatcher {

    private TextView textView;
    private boolean isValid;

    public TextFieldValidator(TextView textView)
    {
        isValid = false;
        this.textView = textView;
    }

    public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4){
    }

    public void onTextChanged(CharSequence var1, int var2, int var3, int var4){
    }

    public void afterTextChanged(Editable var1){
        validate(textView);
    }

    public abstract void validate(TextView textView);

    protected void setValid(boolean isValid)
    {
        this.isValid = isValid;
    }

    public boolean isValid()
    {
        return isValid;
    }
}
