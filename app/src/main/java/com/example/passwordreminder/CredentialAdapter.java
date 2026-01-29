package com.example.passwordreminder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CredentialAdapter extends RecyclerView.Adapter<CredentialAdapter.VH> {

    public interface Actions {
        void onUpdate(Credential c);
        void onDelete(Credential c);
    }

    private final Actions actions;
    private final boolean isAdmin;
    private final List<Credential> data = new ArrayList<>();

    public CredentialAdapter(Actions actions, boolean isAdmin) {
        this.actions = actions;
        this.isAdmin = isAdmin;
    }

    public void submit(List<Credential> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_credential, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Credential c = data.get(position);

        h.tvTitle.setText(c.title);
        h.tvUsername.setText(c.username);

        // Admin değilse butonları gizle
        h.actionsRow.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        h.btnUpdate.setOnClickListener(v -> actions.onUpdate(c));
        h.btnDelete.setOnClickListener(v -> actions.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvUsername;
        Button btnUpdate, btnDelete;
        LinearLayout actionsRow;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            actionsRow = itemView.findViewById(R.id.actionsRow);
        }
    }
}
