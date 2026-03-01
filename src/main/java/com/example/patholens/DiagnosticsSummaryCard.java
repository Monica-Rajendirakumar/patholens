package com.example.patholens;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.patholens.modules.Patient;

import java.util.List;

public class DiagnosticsSummaryCard {

    public static View inflate(Context context, android.view.ViewGroup parent) {
        return LayoutInflater.from(context)
                .inflate(R.layout.item_diagnosis_summary, parent, true);
    }

    public static void populate(View cardRoot, List<Patient> patients) {
        int total        = patients.size();
        int pemphigus    = 0;
        int nonPemphigus = 0;
        int unable       = 0;

        double pemConfSum   = 0; int pemConfCount  = 0;
        double nonConfSum   = 0; int nonConfCount  = 0;
        double allConfSum   = 0; int allConfCount  = 0;

        for (Patient p : patients) {
            String result = p.getResult();
            double conf   = p.getConfidence();

            if (conf == 0) {
                unable++;
            } else {
                boolean isPem = false;
                if (result != null) {
                    String lower = result.toLowerCase();
                    isPem = lower.contains("pemphigus")
                            && !lower.contains("no-pemphigus")
                            && !lower.contains("no pemphigus")
                            && !lower.contains("negative");
                }
                if (isPem) { pemphigus++; pemConfSum += conf; pemConfCount++; }
                else        { nonPemphigus++; nonConfSum += conf; nonConfCount++; }
                allConfSum += conf; allConfCount++;
            }
        }

        double avgPem     = pemConfCount  > 0 ? pemConfSum  / pemConfCount  : 0;
        double avgNonPem  = nonConfCount  > 0 ? nonConfSum  / nonConfCount  : 0;
        double avgOverall = allConfCount  > 0 ? allConfSum  / allConfCount  : 0;

        // Bind
        TextView tvTotal        = cardRoot.findViewById(R.id.tvTotalCount);
        TextView tvPem          = cardRoot.findViewById(R.id.tvPemphigusCount);
        TextView tvNonPem       = cardRoot.findViewById(R.id.tvNonPemphigusCount);
        TextView tvUnable       = cardRoot.findViewById(R.id.tvUnableCount);
        TextView tvPemConf      = cardRoot.findViewById(R.id.tvPemphigusConfidence);
        TextView tvNonPemConf   = cardRoot.findViewById(R.id.tvNonPemphigusConfidence);
        TextView tvAvgConf      = cardRoot.findViewById(R.id.tvAvgConfidence);
        ProgressBar pbPem       = cardRoot.findViewById(R.id.pbPemphigus);
        ProgressBar pbNonPem    = cardRoot.findViewById(R.id.pbNonPemphigus);
        ProgressBar pbUnable    = cardRoot.findViewById(R.id.pbUnable);

        int pemPct    = total > 0 ? Math.round(pemphigus    * 100f / total) : 0;
        int nonPemPct = total > 0 ? Math.round(nonPemphigus * 100f / total) : 0;
        int unablePct = total > 0 ? Math.round(unable       * 100f / total) : 0;

        animateCounter(tvTotal,   total);
        animateCounter(tvPem,     pemphigus);
        animateCounter(tvNonPem,  nonPemphigus);
        animateCounter(tvUnable,  unable);

        animateConfidence(tvPemConf,    avgPem);
        animateConfidence(tvNonPemConf, avgNonPem);
        animateConfidence(tvAvgConf,    avgOverall);

        animateProgressBar(pbPem,    pemPct);
        animateProgressBar(pbNonPem, nonPemPct);
        animateProgressBar(pbUnable, unablePct);

        cardRoot.setAlpha(0f);
        cardRoot.animate().alpha(1f).setDuration(600)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    private static void animateCounter(TextView tv, int target) {
        ValueAnimator a = ValueAnimator.ofInt(0, target);
        a.setDuration(1000);
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(va -> tv.setText(String.valueOf(va.getAnimatedValue())));
        a.start();
    }

    private static void animateConfidence(TextView tv, double target) {
        ValueAnimator a = ValueAnimator.ofFloat(0f, (float) target);
        a.setDuration(1200);
        a.setInterpolator(new DecelerateInterpolator());
        a.addUpdateListener(va -> tv.setText(String.format("%.0f%%", (float) va.getAnimatedValue())));
        a.start();
    }

    private static void animateProgressBar(ProgressBar pb, int target) {
        pb.setMax(100);
        ObjectAnimator a = ObjectAnimator.ofInt(pb, "progress", 0, target);
        a.setDuration(1000);
        a.setInterpolator(new DecelerateInterpolator());
        a.start();
    }
}