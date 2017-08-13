package smart_contract.csg.ifi.uzh.ch.smartcontract.common.controls;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An {@link ImageView} that automatically scales its dimensions based on its intrinsic
 * width/height ratio.
 */
public class ProportionalImageView extends ImageView {

    private ScaleDimension scale = ScaleDimension.Width;

    public ProportionalImageView(Context context) {
        super(context);
    }

    public ProportionalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Determines which dimension of the image should be scaled
     *
     * @param dimension
     */
    public void setScale(ScaleDimension dimension)
    {
        scale = dimension;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = getDrawable();
        if (d != null) {
            if(scale == ScaleDimension.Height)
            {
                int h = MeasureSpec.getSize(heightMeasureSpec);
                int w = h * d.getIntrinsicWidth() / d.getIntrinsicHeight();
                setMeasuredDimension(w, h);
            }else{
                int w = MeasureSpec.getSize(widthMeasureSpec);
                int h = w * d.getIntrinsicHeight() / d.getIntrinsicWidth();
                setMeasuredDimension(w, h);
            }
        }
        else super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public enum ScaleDimension{
        Width,
        Height
    }
}
