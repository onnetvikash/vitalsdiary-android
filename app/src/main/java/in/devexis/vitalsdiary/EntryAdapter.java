package in.devexis.vitalsdiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.devexis.vitalsdiary.core.SymptomEntry;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    private final List<SymptomEntry> entries = new ArrayList<>();

    public void submitList(List<SymptomEntry> newEntries) {
        entries.clear();
        // Show most recent first in the UI (the underlying model stays oldest-first for analysis/export).
        for (int i = newEntries.size() - 1; i >= 0; i--) {
            entries.add(newEntries.get(i));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SymptomEntry entry = entries.get(position);
        String date = DATE_FORMAT.format(Instant.ofEpochMilli(entry.timestampMillis).atZone(ZoneId.systemDefault()));
        holder.date.setText(date);
        holder.severity.setText(holder.itemView.getContext().getString(R.string.severity_format, entry.severity));
        holder.note.setText(entry.note);
        holder.note.setVisibility(entry.note.isEmpty() ? View.GONE : View.VISIBLE);

        StringBuilder tags = new StringBuilder();
        if (entry.hasPhoto) {
            tags.append(holder.itemView.getContext().getString(R.string.tag_photo));
        }
        if (entry.hasVoiceNote) {
            if (tags.length() > 0) {
                tags.append("  ");
            }
            tags.append(holder.itemView.getContext().getString(R.string.tag_voice));
        }
        holder.attachments.setText(tags.toString());
        holder.attachments.setVisibility(tags.length() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView severity;
        final TextView note;
        final TextView attachments;

        ViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.entryDate);
            severity = itemView.findViewById(R.id.entrySeverity);
            note = itemView.findViewById(R.id.entryNote);
            attachments = itemView.findViewById(R.id.entryAttachments);
        }
    }
}
