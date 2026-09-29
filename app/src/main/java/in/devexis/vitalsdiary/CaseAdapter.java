package in.devexis.vitalsdiary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.devexis.vitalsdiary.core.HealthCase;
import in.devexis.vitalsdiary.core.TrendAnalyzer;

public class CaseAdapter extends RecyclerView.Adapter<CaseAdapter.ViewHolder> {

    public interface OnCaseClickListener {
        void onCaseClick(HealthCase healthCase);
    }

    private final List<HealthCase> cases = new ArrayList<>();
    private final OnCaseClickListener listener;

    public CaseAdapter(OnCaseClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<HealthCase> newCases) {
        cases.clear();
        cases.addAll(newCases);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_case, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthCase healthCase = cases.get(position);
        holder.title.setText(healthCase.title);

        int count = healthCase.entries().size();
        String subtitle = holder.itemView.getResources()
                .getQuantityString(R.plurals.entry_count, count, count);
        holder.subtitle.setText(subtitle);

        TrendAnalyzer.Trend trend = TrendAnalyzer.analyze(healthCase.entries());
        holder.trend.setText(trendLabel(trend));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCaseClick(healthCase);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cases.size();
    }

    private String trendLabel(TrendAnalyzer.Trend trend) {
        switch (trend) {
            case IMPROVING:
                return "Improving";
            case WORSENING:
                return "Worsening";
            case STABLE:
                return "Stable";
            default:
                return "Not enough data";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView trend;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.caseTitle);
            subtitle = itemView.findViewById(R.id.caseSubtitle);
            trend = itemView.findViewById(R.id.caseTrend);
        }
    }
}
