package com.cmtech.android.bledeviceapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cmtech.android.bledevice.hrm.activityfragment.HrmFragment;
import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledeviceapp.global.MyApplication;
import com.cmtech.android.bledeviceapp.model.Account;

import java.util.Date;
import java.util.Objects;

import static com.cmtech.android.bledeviceapp.model.Account.FEMALE;
import static com.cmtech.android.bledeviceapp.model.Account.MALE;

/**
 * ProjectName:    BtDeviceApp
 * Package:        com.cmtech.android.bledevice.hrmonitor.view
 * ClassName:      EcgRecordFragment
 * Description:    java类作用描述
 * Author:         作者名
 * CreateDate:     2020/3/28 上午6:48
 * UpdateUser:     更新者
 * UpdateDate:     2020/3/28 上午6:48
 * UpdateRemark:   更新说明
 * Version:        1.0
 */
public class PersonInfoFragment extends Fragment {
    public static final String TITLE = "个人信息";

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private DatePicker dpBirthday;
    private EditText etWeight;
    private EditText etHeight;

    private Account account;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_person_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        account = MyApplication.getAccount();

        rgGender = view.findViewById(R.id.rg_gender);
        rbMale = view.findViewById(R.id.rb_male);
        rbFemale = view.findViewById(R.id.rb_female);
        dpBirthday = view.findViewById(R.id.dp_birthday);
        etWeight = view.findViewById(R.id.et_weight);
        etHeight = view.findViewById(R.id.et_height);

        updateUI();
    }

    public void updateUI() {
        int gender = account.getGender();
        rgGender.clearCheck();
        if(gender == MALE)
            rbMale.setChecked(true);
        else if(gender == FEMALE)
            rbFemale.setChecked(true);

        Date date;
        if(account.getBirthday() <= 0) {
            date = new Date(1990,0,1);
        } else {
            date = new Date(account.getBirthday());
        }
        dpBirthday.init(date.getYear(), date.getMonth(), date.getDate(), null);
        etWeight.setText(String.valueOf(account.getWeight()));
        etHeight.setText(String.valueOf(account.getHeight()));
    }

    public void processOKButton() {
        int weight;
        int height;
        try{
            weight = Integer.parseInt(etWeight.getText().toString().trim());
            height = Integer.parseInt(etHeight.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(getActivity(), "数据格式错误，请重新输入", Toast.LENGTH_SHORT).show();
            return;
        }
        int gender = 0;
        int checkId = rgGender.getCheckedRadioButtonId();
        if(checkId == R.id.rb_male) {
            gender = MALE;
        } else if(checkId == R.id.rb_female) {
            gender = FEMALE;
        }

        Date date = new Date(dpBirthday.getYear(), dpBirthday.getMonth(), dpBirthday.getDayOfMonth());
        account.setPersonInfo(gender, date.getTime(), weight, height);
    }
}
