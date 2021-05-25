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

public class NickChangeDialog extends Dialog {
    private Context context;
    private NickChangeDialogListener nickChangeDialogListener;
    String userNickName;

    EditText et_dialogNick;
    Button bt_dialogChange, bt_dialogCancel;
    TextView tv_dialogIsCheckedNick;

    public NickChangeDialog(Context context, NickChangeDialogListener nickChangeDialogListener) {
        super(context);
        this.context = context;
        this.nickChangeDialogListener = nickChangeDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_nick);

        et_dialogNick = (EditText) findViewById(R.id.nickDialog_nick);
        bt_dialogChange = (Button) findViewById(R.id.nickDialog_btChange);
        bt_dialogCancel = (Button) findViewById(R.id.nickDialog_btCancel);
        tv_dialogIsCheckedNick = (TextView) findViewById(R.id.nickDialog_isCheckedNick);

        //값이 변경될 때,
        et_dialogNick.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) //닉네임 중복 경고 지움
                    tv_dialogIsCheckedNick.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0));
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        //"변경"버튼 눌렀을 때,
        bt_dialogChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_dialogNick.getText().toString().equals("")) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.addRule(RelativeLayout.BELOW, R.id.nickDialog_nick);
                    params.setMargins(45, 10, 45, 0);
                    tv_dialogIsCheckedNick.setLayoutParams(params);
                    tv_dialogIsCheckedNick.setText("변경할 닉네임을 입력하지 않으셨습니다.");
                }
                else {
                    userNickName = et_dialogNick.getText().toString();
                    tv_dialogIsCheckedNick.setText("입력하신 닉네임이 이미 존재합니다.");

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

    public interface NickChangeDialogListener{
        void onClickChangeBt();
    }
}
