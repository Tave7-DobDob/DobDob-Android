package com.tave7.dobdob;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NameChangeDialog extends Dialog {
    private Context context;
    private NameChangeDialogListener nameChangeDialogListener;
    String userName;

    EditText et_dialogName;
    Button bt_dialogChange, bt_dialogCancel;
    TextView tv_dialogIsCheckedName;

    public NameChangeDialog(Context context, NameChangeDialogListener nameChangeDialogListener) {
        super(context);
        this.context = context;
        this.nameChangeDialogListener = nameChangeDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_name);

        et_dialogName = (EditText) findViewById(R.id.nameDialog_name);
        bt_dialogChange = (Button) findViewById(R.id.nameDialog_btChange);
        bt_dialogCancel = (Button) findViewById(R.id.nameDialog_btCancel);
        tv_dialogIsCheckedName = (TextView) findViewById(R.id.nameDialog_isCheckedName);

        //값이 변경될 때,
        et_dialogName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) //닉네임 중복 경고 지움
                    tv_dialogIsCheckedName.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //"변경"버튼 눌렀을 때,
        bt_dialogChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_dialogName.getText().toString().equals("")) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, R.id.nameDialog_name);
                    params.setMargins(45, 10, 45, 0);
                    tv_dialogIsCheckedName.setLayoutParams(params);
                    tv_dialogIsCheckedName.setText("변경할 닉네임을 입력하지 않으셨습니다.");
                }
                else {
                    userName = et_dialogName.getText().toString();
                    tv_dialogIsCheckedName.setText("입력하신 닉네임이 이미 존재합니다.");

                    //TODO: 서버로 입력한 닉네임을 전달해 중복 확인
                    //TODO: 중복이 되지 않았다면 onClickChangeBt();를 호출해 DB에 바뀐 닉네임 저장 and dismiss();
                }
            }
        });

        bt_dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    public interface NameChangeDialogListener{
        void onClickChangeBt();
    }
}
