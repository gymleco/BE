package kr.co.gymleco.support.csv;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class CsvWriter implements AutoCloseable {
    private static final String UTF8_BOM = "\uFEFF";
    private final Writer writer;
    private boolean headerWritten = false;
    public CsvWriter(Writer writer) throws IOException {
        this.writer = writer;
        this.writer.write(UTF8_BOM);
    }
    public void writeRow(List<String> cells) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(cells.get(i)));
        }
        line.append("\r\n");
        writer.write(line.toString());
    }
    public void writeHeader(List<String> headers) throws IOException {
        if (headerWritten) {
            throw new IllegalStateException("헤더는 한 번만 씁니다.");
        }
        writeRow(headers);
        headerWritten = true;
    }
    static String escape(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "\"\"";
        }
        String value = raw.replace('\r', ' ').replace('\n', ' ');
        if (needsFormulaGuard(value)) {
            value = "'" + value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
    private static boolean needsFormulaGuard(String value) {
        char first = value.charAt(0);
        boolean risky = first == '=' || first == '+' || first == '@' || first == '\t' || first == '\r';
        if (first == '-') {
            return !isNumeric(value);
        }
        return risky;
    }
    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @Override
    public void close() throws IOException {
        writer.flush();
        writer.close();
    }
}
