package com.example.patholens;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiagnosisReportActivity extends AppCompatActivity {

    private static final String TAG = "DiagnosisReport";
    private static final int HIGH_CONFIDENCE_THRESHOLD = 90;
    private static final int REQUEST_WRITE_STORAGE = 101;

    // PDF page size (A4 at 72 dpi)
    private static final int PAGE_WIDTH  = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN      = 50;
    private static final int LINE_HEIGHT = 26;

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnDownload;
    private TextView tvDiagnosisTitle;
    private TextView tvPatientName;
    private TextView tvPatientAge;
    private TextView tvPatientGender;
    private TextView tvDiagnosisDate;
    private TextView tvDiagnosisResult;
    private TextView tvConfidenceScore;
    private TextView tvConfidencePercentage;
    private TextView tvRecommendations;
    private TextView tvNotes;
    private CardView cardPatientInfo;
    private CardView cardDiagnosisInfo;
    private CardView cardRecommendations;

    private String  diagnosisId;
    private String  patientName;
    private String  gender;
    private String  date;
    private int     age;
    private int     confidence;
    private boolean isPemphigus;

    // ─────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_report);
        initializeViews();
        loadDataFromIntent();
        displayReportData();
        setupClickListeners();
    }

    // ─────────────────────────────────────────────────────────
    // Init
    // ─────────────────────────────────────────────────────────

    private void initializeViews() {
        btnBack                = findViewById(R.id.btnBack);
        btnShare               = findViewById(R.id.btnShare);
        btnDownload            = findViewById(R.id.btnDownload);
        tvDiagnosisTitle       = findViewById(R.id.tvDiagnosisTitle);
        tvPatientName          = findViewById(R.id.tvPatientName);
        tvPatientAge           = findViewById(R.id.tvPatientAge);
        tvPatientGender        = findViewById(R.id.tvPatientGender);
        tvDiagnosisDate        = findViewById(R.id.tvDiagnosisDate);
        tvDiagnosisResult      = findViewById(R.id.tvDiagnosisResult);
        tvConfidenceScore      = findViewById(R.id.tvConfidenceScore);
        tvConfidencePercentage = findViewById(R.id.tvConfidencePercentage);
        tvRecommendations      = findViewById(R.id.tvRecommendations);
        tvNotes                = findViewById(R.id.tvNotes);
        cardPatientInfo        = findViewById(R.id.cardPatientInfo);
        cardDiagnosisInfo      = findViewById(R.id.cardDiagnosisInfo);
        cardRecommendations    = findViewById(R.id.cardRecommendations);
    }

    private void loadDataFromIntent() {
        Intent i    = getIntent();
        diagnosisId = i.getStringExtra("diagnosis_id");
        patientName = i.getStringExtra("name");
        age         = i.getIntExtra("age", 0);
        date        = i.getStringExtra("date");
        gender      = i.getStringExtra("gender");

        // Confidence is stored as whole number (e.g. 87), get it directly
        confidence = i.getIntExtra("confidence", 0);

        // Fix "no-pemphigus" showing as pemphigus — derive from result label
        String resultLabel = i.getStringExtra("result");
        if (resultLabel != null) {
            String lower = resultLabel.toLowerCase();
            isPemphigus = lower.contains("pemphigus")
                    && !lower.contains("no-pemphigus")
                    && !lower.contains("no pemphigus")
                    && !lower.contains("negative");
        } else {
            isPemphigus = i.getBooleanExtra("pemphigus", false);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Display
    // ─────────────────────────────────────────────────────────

    private void displayReportData() {
        tvDiagnosisTitle.setText(diagnosisId != null ? diagnosisId : "Diagnosis Report");
        tvPatientName.setText(patientName != null ? patientName : "N/A");
        tvPatientAge.setText(age + " years");
        tvPatientGender.setText(gender != null ? capitalizeFirst(gender) : "N/A");
        tvDiagnosisDate.setText(date != null ? date : "N/A");

        String resultText = isPemphigus ? "Pemphigus Detected" : "No Pemphigus Detected";
        tvDiagnosisResult.setText(resultText);
        tvDiagnosisResult.setTextColor(getResources().getColor(
                isPemphigus ? android.R.color.holo_red_dark : android.R.color.holo_green_dark));

        // confidence == 0 → "Unable to classify"
        if (confidence == 0) {
            tvConfidenceScore.setText("N/A");
            tvConfidencePercentage.setText("Confidence: Unable to classify");
        } else {
            tvConfidenceScore.setText(confidence + "%");
            tvConfidencePercentage.setText("Confidence: " + confidence + "%");
        }

        tvRecommendations.setText(getRecommendations(isPemphigus, confidence));
        tvNotes.setText(getClinicalNotes(isPemphigus));
    }

    // ─────────────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────────────

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v ->
                Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show());
        btnDownload.setOnClickListener(v -> checkPermissionAndDownload());
    }

    // ─────────────────────────────────────────────────────────
    // Permission
    // ─────────────────────────────────────────────────────────

    private void checkPermissionAndDownload() {
        // Android 10+ uses MediaStore — no WRITE permission needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadReport();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadReport();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadReport();
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    // ─────────────────────────────────────────────────────────
    // PDF generation
    // ─────────────────────────────────────────────────────────

    private void downloadReport() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName  = "PathoLens_Report_" + timestamp + ".pdf";

        // Build the PDF in memory first
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        drawPdfContent(page.getCanvas());
        document.finishPage(page);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(document, fileName);   // Android 10+
            } else {
                saveViaFileSystem(document, fileName);   // Android 9 and below
            }
        } catch (Exception e) {
            Log.e(TAG, "PDF save failed", e);
            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }

    /**
     * Android 10+ — use MediaStore.Downloads so no storage permission is required.
     */
    private void saveViaMediaStore(PdfDocument document, String fileName) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
        values.put(MediaStore.Downloads.IS_PENDING, 1);

        Uri uri = getContentResolver().insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IOException("MediaStore returned null URI");

        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new IOException("Null output stream for URI");
            document.writeTo(out);
        }

        // Mark as complete so it becomes visible in Downloads
        values.clear();
        values.put(MediaStore.Downloads.IS_PENDING, 0);
        getContentResolver().update(uri, values, null, null);

        Toast.makeText(this, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
        openPdf(uri);
    }

    /**
     * Android 9 and below — write directly to the public Downloads folder.
     */
    private void saveViaFileSystem(PdfDocument document, String fileName) throws IOException {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create Downloads directory");
        }
        File pdfFile = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            document.writeTo(fos);
        }
        Toast.makeText(this, "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);
        openPdf(uri);
    }

    private void openPdf(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.w(TAG, "No PDF viewer found: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // PDF drawing helpers
    // ─────────────────────────────────────────────────────────

    private void drawPdfContent(Canvas canvas) {
        // ── Header bar ──────────────────────────────────────
        Paint hdrBg = solidPaint(Color.parseColor("#1A73E8"));
        canvas.drawRect(0, 0, PAGE_WIDTH, 72, hdrBg);
        canvas.drawText("PathoLens",
                MARGIN, 40, makePaint(Color.WHITE, 22, true));
        canvas.drawText("AI-Powered Pemphigus Diagnosis Report",
                MARGIN, 62, makePaint(Color.WHITE, 12, false));

        int y = 92;

        // ── Report meta ──────────────────────────────────────
        Paint gray  = makePaint(Color.GRAY,  10, false);
        Paint black = makePaint(Color.BLACK, 11, false);
        canvas.drawText("Report ID",    MARGIN,          y, gray);
        canvas.drawText("Generated On", PAGE_WIDTH / 2,  y, gray);
        y += 17;
        canvas.drawText(diagnosisId != null ? diagnosisId : "Diagnosis Report",
                MARGIN, y, makePaint(Color.BLACK, 11, true));
        canvas.drawText(
                new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()),
                PAGE_WIDTH / 2, y, black);
        y += 22;
        drawDivider(canvas, y); y += 18;

        // ── Patient Information ──────────────────────────────
        y = drawSectionHeader(canvas, "Patient Information", y);
        y = drawRow(canvas, "Name",   patientName != null ? patientName : "N/A", y);
        y = drawRow(canvas, "Age",    age + " years", y);
        y = drawRow(canvas, "Gender", gender != null ? capitalizeFirst(gender) : "N/A", y);
        y += 6; drawDivider(canvas, y); y += 18;

        // ── Diagnosis Information ────────────────────────────
        y = drawSectionHeader(canvas, "Diagnosis Information", y);
        y = drawRow(canvas, "Date", date != null ? date : "N/A", y);

        canvas.drawText("Result", MARGIN, y, gray); y += 16;
        int    rColor = isPemphigus ? Color.RED : Color.parseColor("#2E7D32");
        String rText  = isPemphigus ? "Pemphigus Detected" : "No Pemphigus Detected";
        canvas.drawText(rText, MARGIN, y, makePaint(rColor, 11, true));
        y += LINE_HEIGHT;

        String confDisplay = (confidence == 0) ? "Unable to classify" : confidence + "%";
        y = drawRow(canvas, "Confidence Score", confDisplay, y);
        y += 6; drawDivider(canvas, y); y += 18;

        // ── Recommendations ──────────────────────────────────
        y = drawSectionHeader(canvas, "Recommendations", y);
        for (String rec : getRecommendations(isPemphigus, confidence).split("\n")) {
            canvas.drawText(rec, MARGIN, y, black);
            y += LINE_HEIGHT;
        }
        y += 6; drawDivider(canvas, y); y += 18;

        // ── Clinical Notes ───────────────────────────────────
        y = drawSectionHeader(canvas, "Clinical Notes", y);
        drawWrappedText(canvas, getClinicalNotes(isPemphigus), y, black);

        // ── Footer ───────────────────────────────────────────
        canvas.drawRect(0, PAGE_HEIGHT - 42, PAGE_WIDTH, PAGE_HEIGHT,
                solidPaint(Color.parseColor("#F5F5F5")));
        Paint ft = makePaint(Color.GRAY, 8, false);
        canvas.drawText(
                "This report is AI-generated and does not replace professional medical advice.",
                MARGIN, PAGE_HEIGHT - 24, ft);
        canvas.drawText("Page 1", PAGE_WIDTH - MARGIN - 36, PAGE_HEIGHT - 24, ft);
    }

    private int drawSectionHeader(Canvas canvas, String title, int y) {
        canvas.drawRect(MARGIN - 8, y - 15, PAGE_WIDTH - MARGIN + 8, y + 7,
                solidPaint(Color.parseColor("#E8F0FE")));
        canvas.drawText(title, MARGIN, y,
                makePaint(Color.parseColor("#1A73E8"), 12, true));
        return y + LINE_HEIGHT + 4;
    }

    private int drawRow(Canvas canvas, String label, String value, int y) {
        canvas.drawText(label, MARGIN, y, makePaint(Color.GRAY, 10, false));
        y += 15;
        canvas.drawText(value, MARGIN, y, makePaint(Color.BLACK, 11, false));
        return y + LINE_HEIGHT;
    }

    private void drawDivider(Canvas canvas, int y) {
        Paint p = new Paint();
        p.setColor(Color.parseColor("#E0E0E0"));
        p.setStrokeWidth(1f);
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, p);
    }

    private int drawWrappedText(Canvas canvas, String text, int y, Paint paint) {
        float maxW = PAGE_WIDTH - 2f * MARGIN;
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (paint.measureText(test) > maxW) {
                canvas.drawText(line.toString(), MARGIN, y, paint);
                y += LINE_HEIGHT;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) { canvas.drawText(line.toString(), MARGIN, y, paint); y += LINE_HEIGHT; }
        return y;
    }

    private Paint makePaint(int color, float sp, boolean bold) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setTextSize(sp * 1.5f);
        if (bold) p.setTypeface(Typeface.DEFAULT_BOLD);
        return p;
    }

    private Paint solidPaint(int color) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    // ─────────────────────────────────────────────────────────
    // Content helpers
    // ─────────────────────────────────────────────────────────

    private String getRecommendations(boolean isPemphigus, int confidence) {
        if (isPemphigus) {
            return confidence >= HIGH_CONFIDENCE_THRESHOLD
                    ? "• Immediate consultation with a dermatologist is strongly recommended\n" +
                    "• Consider starting corticosteroid therapy\n" +
                    "• Perform biopsy for confirmation\n" +
                    "• Monitor for secondary infections\n" +
                    "• Regular follow-up appointments required"
                    : "• Consult with a dermatologist for further evaluation\n" +
                    "• Additional diagnostic tests may be required\n" +
                    "• Monitor symptoms closely\n" +
                    "• Schedule follow-up examination";
        }
        return "• Continue routine skin care practices\n" +
                "• Monitor for any changes in skin condition\n" +
                "• Schedule regular check-ups as needed\n" +
                "• Maintain good hygiene practices";
    }

    private String getClinicalNotes(boolean isPemphigus) {
        return isPemphigus
                ? "Pemphigus is an autoimmune disorder that causes blistering of the skin and mucous membranes. " +
                "Early diagnosis and treatment are crucial for better outcomes. This AI-assisted diagnosis should " +
                "be confirmed by a qualified healthcare professional through clinical examination and laboratory tests."
                : "No signs of pemphigus detected in the analysis. However, this AI-assisted diagnosis should not " +
                "replace professional medical consultation. If you experience any concerning symptoms, please consult " +
                "a healthcare provider for proper evaluation.";
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}