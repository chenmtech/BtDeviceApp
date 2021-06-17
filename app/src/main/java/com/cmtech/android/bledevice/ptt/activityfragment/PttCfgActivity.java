package com.cmtech.android.bledevice.ptt.activityfragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmtech.android.bledevice.ptt.model.PttCfg;
import com.cmtech.android.bledeviceapp.R;

public class PttCfgActivity extends AppCompatActivity {
    private static final int HR_LIMIT_INTERVAL = 5;
    private PttCfg pttCfg;
    private EditText etPtt0;
    private EditText etSbp0;
    private EditText etDbp0;

    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cfg_hrm);

        // create ToolBar
        Toolbar toolbar = findViewById(R.id.tb_hrm_cfg);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if(intent != null) {
            pttCfg = (PttCfg) intent.getSerializableExtra("ptt_cfg");
        }

        etPtt0 = findViewById(R.id.et_ptt0);
        etSbp0 = findViewById(R.id.et_sbp0);
        etDbp0 = findViewById(R.id.et_dbp0);

        etPtt0.setText(String.valueOf(pttCfg.getPtt0()));
        etSbp0.setText(String.valueOf(pttCfg.getSbp0()));
        etDbp0.setText(String.valueOf(pttCfg.getDbp0()));

        btnOk = findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int ptt0 = Integer.parseInt(etPtt0.getText().toString());
                    int sbp0 = Integer.parseInt(etSbp0.getText().toString());
                    int dbp0 = Integer.parseInt(etDbp0.getText().toString());
                    pttCfg.setPtt0(ptt0);
                    pttCfg.setSbp0(sbp0);
                    pttCfg.setDbp0(dbp0);
                    Intent intent = new Intent();
                    intent.putExtra("ptt_cfg", pttCfg);
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (NumberFormatException ignored) {
                    Toast.makeText(PttCfgActivity.this, "值无效，请重设！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED, null);
                finish();
                break;
        }
        return true;
    }

}
