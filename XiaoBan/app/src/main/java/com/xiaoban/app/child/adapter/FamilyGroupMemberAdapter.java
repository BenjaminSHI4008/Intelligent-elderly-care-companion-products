package com.xiaoban.app.child.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.model.FamilyGroupMember;

import java.util.ArrayList;
import java.util.List;

public class FamilyGroupMemberAdapter extends RecyclerView.Adapter<FamilyGroupMemberAdapter.ViewHolder> {

    private List<FamilyGroupMember> members = new ArrayList<>();

    public void setMembers(List<FamilyGroupMember> members) {
        this.members = members != null ? members : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_family_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvName;
        private final View elderRing;
        private final TextView tvElderBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            elderRing = itemView.findViewById(R.id.elder_ring);
            tvElderBadge = itemView.findViewById(R.id.tv_elder_badge);
        }

        void bind(FamilyGroupMember member) {
            tvName.setText(member.getNickname());
            tvAvatar.setText(member.getAvatarText());
            boolean isElder = member.isElder();
            elderRing.setVisibility(isElder ? View.VISIBLE : View.GONE);
            tvElderBadge.setVisibility(isElder ? View.VISIBLE : View.GONE);
        }
    }
}
