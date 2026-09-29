package in.devexis.vitalsdiary;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import in.devexis.vitalsdiary.core.HealthCase;
import in.devexis.vitalsdiary.core.TimelineTextBuilder;

/**
 * Renders a HealthCase's timeline summary into a real PDF using Android's built-in
 * PdfDocument API. No external library, no internet access required.
 */
public final class PdfExporter {

    private static final int PAGE_WIDTH = 595;  // A4 at 72dpi
    private static final int PAGE_HEIGHT = 842;
    private static final int MARGIN = 40;
    private static final int LINE_HEIGHT = 16;
    private static final int MAX_CHARS_PER_LINE = 90;

    private PdfExporter() {}

    public static File export(Context context, HealthCase healthCase) throws IOException {
        String summary = TimelineTextBuilder.buildSummary(healthCase, ZoneId.systemDefault());
        List<String> lines = wrapLines(summary, MAX_CHARS_PER_LINE);

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(12f);
        paint.setAntiAlias(true);

        int linesPerPage = Math.max(1, (PAGE_HEIGHT - MARGIN * 2) / LINE_HEIGHT);
        int pageNumber = 1;
        PdfDocument.Page page = null;
        Canvas canvas = null;

        for (int i = 0; i < lines.size(); i++) {
            if (i % linesPerPage == 0) {
                if (page != null) {
                    document.finishPage(page);
                }
                PdfDocument.PageInfo pageInfo =
                        new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                pageNumber++;
            }
            int y = MARGIN + (i % linesPerPage) * LINE_HEIGHT;
            canvas.drawText(lines.get(i), MARGIN, y, paint);
        }
        if (page != null) {
            document.finishPage(page);
        }

        File dir = new File(context.getCacheDir(), "exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String safeTitle = healthCase.title.replaceAll("[^a-zA-Z0-9]+", "_");
        File file = new File(dir, "VitalsDiary_" + safeTitle + ".pdf");
        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
        }
        document.close();
        return file;
    }

    private static List<String> wrapLines(String text, int maxCharsPerLine) {
        List<String> result = new ArrayList<>();
        for (String rawLine : text.split("\n", -1)) {
            if (rawLine.isEmpty()) {
                result.add("");
                continue;
            }
            StringBuilder current = new StringBuilder();
            for (String word : rawLine.split(" ")) {
                if (current.length() + word.length() + 1 > maxCharsPerLine && current.length() > 0) {
                    result.add(current.toString());
                    current = new StringBuilder();
                }
                if (current.length() > 0) {
                    current.append(' ');
                }
                current.append(word);
            }
            result.add(current.toString());
        }
        return result;
    }
}
