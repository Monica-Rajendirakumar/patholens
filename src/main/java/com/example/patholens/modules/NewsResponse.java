package com.example.patholens.modules;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("results")
    private List<NewsArticle> results;

    public String getStatus() {
        return status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<NewsArticle> getResults() {
        return results;
    }

    public static class NewsArticle {
        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("pubDate")
        private String pubDate;

        @SerializedName("link")
        private String link;

        @SerializedName("source_id")
        private String sourceId;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getPubDate() {
            return pubDate;
        }

        public String getLink() {
            return link;
        }

        public String getSourceId() {
            return sourceId;
        }
    }
}