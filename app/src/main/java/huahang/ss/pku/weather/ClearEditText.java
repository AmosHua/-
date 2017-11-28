package huahang.ss.pku.weather;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
/**
 * Created by amoshua on 28/11/2017.
 */

public class ClearEditText extends android.support.v7.widget.AppCompatEditText implements View.OnFocusChangeListener,TextWatcher{
    //删除按钮引用
    private Drawable mClearDrawable;

    public ClearEditText(Context context) {
        super(context);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs,android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mClearDrawable=getCompoundDrawables()[2];
        if(mClearDrawable==null){
            mClearDrawable=getResources().getDrawable(R.drawable.cancel);
        }
        mClearDrawable.setBounds(0,0,mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    protected void setClearIconVisible(boolean visible){
        Drawable right=visible?mClearDrawable:null;
        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],right,getCompoundDrawables()[3]);

    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        setClearIconVisible(s.length()>0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus) {
            setClearIconVisible(getText().length() > 0);
        }else{
            setClearIconVisible(false);
        }
    }
}
