package com.example.patholens.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.patholens.R;
import com.example.patholens.modules.NewsResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsResponse.NewsArticle> newsList;

    public NewsAdapter(Context context, List<NewsResponse.NewsArticle> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsResponse.NewsArticle article = newsList.get(position);

        // Set title
        holder.newsTitle.setText(article.getTitle() != null ? article.getTitle() : "No Title");

        // Set description
        String desc = article.getDescription();
        if (desc != null && !desc.isEmpty()) {
            holder.newsDescription.setText(desc);
        } else {
            holder.newsDescription.setText("No description available");
        }

        // Set time ago
        holder.newsTime.setText(getTimeAgo(article.getPubDate()));

        // Click listener to open article in browser
        holder.newsCard.setOnClickListener(v -> {
            if (article.getLink() != null && !article.getLink().isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getLink()));
                context.startActivity(browserIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    private String getTimeAgo(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) {
            return "Recently";
        }

        try {
            // Try multiple date formats
            SimpleDateFormat[] formats = {
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.US)
            };

            Date publishDate = null;
            for (SimpleDateFormat sdf : formats) {
                try {
                    publishDate = sdf.parse(pubDate);
                    if (publishDate != null) break;
                } catch (ParseException ignored) {}
            }

            if (publishDate != null) {
                long diffInMillis = System.currentTimeMillis() - publishDate.getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
                long months = days / 30;
                long weeks = days / 7;

                if (months > 0) {
                    return months + "mo";
                } else if (weeks > 0) {
                    return weeks + "w";
                } else if (days > 0) {
                    return days + "d";
                } else {
                    return "Today";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Recently";
    }

    public void updateNews(List<NewsResponse.NewsArticle> newNewsList) {
        this.newsList = newNewsList;
        notifyDataSetChanged();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        CardView newsCard;
        TextView newsTitle, newsDescription, newsTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsCard = itemView.findViewById(R.id.news_card);
            newsTitle = itemView.findViewById(R.id.news_title);
            newsDescription = itemView.findViewById(R.id.news_description);
            newsTime = itemView.findViewById(R.id.news_time);
        }
    }
}