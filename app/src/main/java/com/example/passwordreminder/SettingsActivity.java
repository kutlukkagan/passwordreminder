package com.example.passwordreminder;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * ✅ Ayarlar ekranı:
 * - Timeout süresi (2 / 5 / 10 dk)
 * - (Tema vs. sonra eklenebilir)
 */
public class SettingsActivity extends BaseSecureActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // ✅ Screenshot + screen record engelle
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
        );

        super.onCreate(savedInstanceState);

        // ✅ Oturum yoksa direkt kapat
        if (SessionManager.get().getCurrentUser() == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_settings);

        // ✅ Toolbar bağla + geri ok
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ayarlar");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            toolbar.setTitle("Ayarlar");
        }

        RadioGroup rg = findViewById(R.id.rgTimeout);
        Button btnSave = findViewById(R.id.btnSaveSettings);

        // ✅ Kayıtlı timeout değerini UI'da seçili yap
        int currentMin = SettingsPrefs.getTimeoutMin(this);
        checkRadioByMinutes(rg, currentMin);

        // ✅ Kaydet
        btnSave.setOnClickListener(v -> {
            int minutes = getSelectedMinutes(rg);
            SettingsPrefs.setTimeoutMin(this, minutes);

            // ✅ SessionManager anında etkilesin diye touch atıyoruz
            SessionManager.get().touch();

            Toast.makeText(this, "Kaydedildi (" + minutes + " dk)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // ✅ Toolbar geri ok
        finish();
        return true;
    }

    private int getSelectedMinutes(RadioGroup rg) {
        int id = rg.getCheckedRadioButtonId();
        if (id == R.id.rb2) return 2;
        if (id == R.id.rb5) return 5;
        if (id == R.id.rb10) return 10;
        return 2; // fallback
    }

    private void checkRadioByMinutes(RadioGroup rg, int min) {
        if (min == 5) rg.check(R.id.rb5);
        else if (min == 10) rg.check(R.id.rb10);
        else rg.check(R.id.rb2);
    }
}
