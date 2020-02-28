package com.savor.ads.customview;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.savor.ads.R;

public class IPEditText extends LinearLayout {

    private EditText mFirstIPTv;
    private EditText mSecondIPTv;
    private EditText mThirdIPTv;
    private EditText mFourthIPTv;

    public IPEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.custom_my_edittext, this);
        mFirstIPTv = (EditText) findViewById(R.id.ip_first);
        mSecondIPTv = (EditText) findViewById(R.id.ip_second);
        mThirdIPTv = (EditText) findViewById(R.id.ip_third);
        mFourthIPTv = (EditText) findViewById(R.id.ip_fourth);
    }

    private int focusedIndex;

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

        if (gainFocus) {
            switch (focusedIndex) {
                case 0:
                    mFirstIPTv.requestFocus();
                    break;
                case 1:
                    mSecondIPTv.requestFocus();
                    break;
                case 2:
                    mThirdIPTv.requestFocus();
                    break;
                case 3:
                    mFourthIPTv.requestFocus();
                    break;
            }
        }
    }

    public void setListener() {
        mFirstIPTv.setOnFocusChangeListener(mOnEtFocusChangeListener);
        mSecondIPTv.setOnFocusChangeListener(mOnEtFocusChangeListener);
        mThirdIPTv.setOnFocusChangeListener(mOnEtFocusChangeListener);
        mFourthIPTv.setOnFocusChangeListener(mOnEtFocusChangeListener);

        mFirstIPTv.addTextChangedListener(mTextWatcher);
        mSecondIPTv.addTextChangedListener(mTextWatcher);
        mThirdIPTv.addTextChangedListener(mTextWatcher);
        mFourthIPTv.addTextChangedListener(mTextWatcher);

    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int focusOrientation = 0;
            if (s != null && s.length() > 0) {
                if (s.length() > 2) {
                    String text = s.toString().trim();
                    try {
                        if (Integer.parseInt(text) > 255) {
                            Toast.makeText(getContext(), "每一段IP不能大于255",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (count > 0) {
                                focusOrientation = 1;
                            }
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                focusOrientation = -1;
            }

            switch (focusedIndex) {
                case 0:
                    if (focusOrientation > 0) {
                        mSecondIPTv.requestFocus();
                    }
                    break;
                case 1:
                    if (focusOrientation > 0) {
                        mThirdIPTv.requestFocus();
                    } else if (focusOrientation < 0) {
                        mFirstIPTv.requestFocus();
                        mFirstIPTv.setSelection(mFirstIPTv.getText().length());
                    }
                    break;
                case 2:
                    if (focusOrientation > 0) {
                        mFourthIPTv.requestFocus();
                    } else if (focusOrientation < 0) {
                        mSecondIPTv.requestFocus();
                        mSecondIPTv.setSelection(mSecondIPTv.getText().length());
                    }
                    break;
                case 3:
                    if (focusOrientation < 0) {
                        mThirdIPTv.requestFocus();
                        mThirdIPTv.setSelection(mThirdIPTv.getText().length());
                    }
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private OnFocusChangeListener mOnEtFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                switch (v.getId()) {
                    case R.id.ip_first:
                        focusedIndex = 0;
                        break;
                    case R.id.ip_second:
                        focusedIndex = 1;
                        break;
                    case R.id.ip_third:
                        focusedIndex = 2;
                        break;
                    case R.id.ip_fourth:
                        focusedIndex = 3;
                        break;
                }
            }
        }
    };

    public String getText() {
        String first = mFirstIPTv.getText().toString().trim();
        String second = mSecondIPTv.getText().toString().trim();
        String third = mThirdIPTv.getText().toString().trim();
        String fourth = mFourthIPTv.getText().toString().trim();

        if (TextUtils.isEmpty(first) || TextUtils.isEmpty(second)
                || TextUtils.isEmpty(third) || TextUtils.isEmpty(fourth)) {
            Toast.makeText(getContext(), "请输入合法的ip地址", Toast.LENGTH_SHORT).show();
            return "";
        }

        return first + "." + second + "." + third + "." + fourth;
    }

    public void setText(String firstIP, String secondIP, String thirdIP, String fourthIP) {
        mFirstIPTv.setText(firstIP);
        mSecondIPTv.setText(secondIP);
        mThirdIPTv.setText(thirdIP);
        mFourthIPTv.setText(fourthIP);
    }
}
