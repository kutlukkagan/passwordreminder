package com.example.passwordreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CredentialAdapter extends RecyclerView.Adapter<CredentialAdapter.VH> {

    // ✅ MainActivity'den gelen aksiyonlar
    public interface Actions {
        void onUpdate(Credential c);
        void onDelete(Credential c);
    }

    private final Actions actions;
    private final boolean isAdmin;

    // ✅ Liste verisi
    private final List<Credential> data = new ArrayList<>();

    // ✅ Parolası açık olan kayıtların ID'leri
    private final Set<Long> revealedIds = new HashSet<>();

    public CredentialAdapter(Actions actions, boolean isAdmin) {
        this.actions = actions;
        this.isAdmin = isAdmin;
    }

    // ✅ Listeyi yenile
    public void submit(List<Credential> list) {
        data.clear();
        if (list != null) data.addAll(list);
        revealedIds.clear(); // liste yenilenince hepsi tekrar maskelensin
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_credential, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Credential c = data.get(position);

        // ✅ Başlık + kullanıcı adı
        h.tvTitle.setText(c.title);
        h.tvUsername.setText(c.username);

        // ✅ Admin değilse Güncelle / Sil satırı gizlenir
        h.actionsRow.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        // ✅ Parola göster / gizle durumu
        boolean revealed = revealedIds.contains(c.id);

        if (revealed) {
            // 🔓 Açık
            h.tvPassword.setText(c.password);
            h.btnTogglePass.setIconResource(R.drawable.ic_visibility_off);
        } else {
            // 🔒 Maskeli
            h.tvPassword.setText("••••••");
            h.btnTogglePass.setIconResource(R.drawable.ic_visibility);
        }

        // 👁 Toggle click
        h.btnTogglePass.setOnClickListener(v -> {
            if (revealedIds.contains(c.id)) {
                revealedIds.remove(c.id);
            } else {
                revealedIds.add(c.id);
            }
            notifyItemChanged(h.getAdapterPosition());
        });

        // ✅ Güncelle / Sil
        h.btnUpdate.setOnClickListener(v -> actions.onUpdate(c));
        h.btnDelete.setOnClickListener(v -> actions.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ===============================
    // ViewHolder
    // ===============================
    static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle, tvUsername, tvPassword;
        MaterialButton btnUpdate, btnDelete, btnTogglePass;
        LinearLayout actionsRow;

        VH(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPassword = itemView.findViewById(R.id.tvPassword);

            btnTogglePass = itemView.findViewById(R.id.btnTogglePass);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            actionsRow = itemView.findViewById(R.id.actionsRow);
        }
    }
}
