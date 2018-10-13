package com.cmtech.android.bledevice.thermo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cmtech.android.bledeviceapp.R;
import com.cmtech.android.bledevicecore.model.BleDeviceFragment;


/**
 * Created by bme on 2018/2/27.
 */

public class ThermoFragment extends BleDeviceFragment implements IThermoDataObserver{


    private TextView tvThermoCurrentTemp;
    private TextView tvThermoHightestTemp;
    private TextView tvThermoStatus;
    private Button btnThermoResetHighestTemp;


    public ThermoFragment() {

    }

    public static ThermoFragment newInstance() {
        return new ThermoFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((ThermoDevice)device).registerThermoDataObserver(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_thermometer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvThermoCurrentTemp = view.findViewById(R.id.tv_thermo_currenttempvalue);
        tvThermoHightestTemp = view.findViewById(R.id.tv_thermo_highesttempvalue);
        tvThermoStatus = view.findViewById(R.id.tv_thermo_status);
        btnThermoResetHighestTemp = view.findViewById(R.id.btn_thermo_resethighesttemp);

        btnThermoResetHighestTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetHighestTemp();
            }
        });
    }

    private void resetHighestTemp() {
        ((ThermoDevice)device).resetHighestTemp();
    }


    @Override
    public void updateThermoData() {
        double curTemp = ((ThermoDevice)device).getCurTemp();
        double highestTemp = ((ThermoDevice)device).getHighestTemp();

        if(curTemp < 34.00) {
            tvThermoCurrentTemp.setText("<34.0");
        }
        else {
            String str = String.format("%.2f", curTemp);
            tvThermoCurrentTemp.setText(str);
        }

        if(highestTemp < 34.00) {
            tvThermoHightestTemp.setText("<34.0");
        }
        else {
            String str = String.format("%.2f", highestTemp);
            tvThermoHightestTemp.setText(str);
        }


        if(highestTemp < 37.0) {
            tvThermoStatus.setText("正常");
        } else if(highestTemp < 38.0) {
            tvThermoStatus.setText("低烧，请注意休息！");
        } else if(highestTemp < 38.5) {
            tvThermoStatus.setText("体温异常，请注意降温！");
        } else {
            tvThermoStatus.setText("高烧，请及时就医！");
        }
    }
}
