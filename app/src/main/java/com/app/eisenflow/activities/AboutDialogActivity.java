package com.app.eisenflow.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.app.eisenflow.R;
import com.app.eisenflow.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 2/24/18.
 */

public class AboutDialogActivity extends AppCompatActivity {
    @BindView(R.id.version) TextView mVersionView;
    @BindView(R.id.about_ok) Button mOkButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_dialog);
        ButterKnife.bind(this);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;

            mVersionView.setText(mVersionView.getText() + version + " " + verCode);
        }
        catch (PackageManager.NameNotFoundException ex) {
            Log.e(Constants.TAG, "AboutDialogException: " + ex.getMessage());
        }
    }

    @OnClick (R.id.about_ok)
    public void onOkClicked() {
        finish();
    }
}
