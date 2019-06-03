package com.xpinjection.hr.feedback;

import com.xpinjection.csv.CsvReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.xpinjection.hr.feedback.CsvFeedbackFormHeaders.*;
import static java.util.stream.Collectors.joining;

public class CsvFeedbackForm {
    private static final Logger LOG = LoggerFactory.getLogger(CsvFeedbackForm.class);

    private static final BiFunction<CsvReader, String, String> SAFE_READ_VALUE = (reader, header) -> {
        try {
            return reader.get(header);
        } catch (IOException e) {
            throw new IllegalStateException("Can't read value for header: " + header, e);
        }
    };
    private static final String SEPARATOR = " | ";
    private static final String YES = "Yes";

    private final File feedbackFile;
    private final Properties config;

    public CsvFeedbackForm(File feedbackFile, String format) {
        this.feedbackFile = feedbackFile;
        try {
            this.config = PropertiesLoaderUtils.loadAllProperties(format + "-config.properties");
        } catch (IOException e) {
            throw new IllegalStateException("Can't read config for format: " + format, e);
        }
    }

    public Set<FeedbackSummary> aggregateFeedbackSummary() {
        Map<String, FeedbackSummary> feedback = new HashMap<>();
        try (InputStream csv = new FileInputStream(feedbackFile)) {
            CsvReader reader = new CsvReader(csv, ',', Charset.forName("UTF-8"));
            reader.readHeaders();
            while (reader.readRecord()) {
                Feedback record = readFeedback(reader);
                addFeedbackRecord(feedback, record);
            }
            reader.close();
        } catch (IOException e) {
            throw new IllegalStateException("Can't process feedback from file", e);
        }
        return new HashSet<>(feedback.values());
    }

    private Feedback readFeedback(CsvReader reader) {
        String author = extractPersonName(reader, formHeader(AUTHOR));
        String employee = extractPersonName(reader, formHeader(FEEDBACK_FOR));
        LOG.info("Start processing feedback from: {}", author);
        Feedback feedback = new Feedback(author);
        feedback.setEmployee(employee);
        feedback.setPeriod(readValue(reader, formHeader(FEEDBACK_PERIOD)));
        if (YES.equals(readValue(reader, formHeader(ANONYMOUS_CHECK)))) {
            feedback.setAnonymous(false);
        }
        fillMarks(reader, feedback);
        feedback.setGood(joinValues(reader, formHeader(WHAT_IS_GOOD)));
        feedback.setImprovements(joinValues(reader, formHeader(WHAT_TO_IMPROVE)));
        return feedback;
    }

    private void addFeedbackRecord(Map<String, FeedbackSummary> feedback, Feedback record) {
        String employee = record.getEmployee();
        FeedbackSummary totalFeedback = feedback.computeIfAbsent(employee, key -> new FeedbackSummary(employee));
        totalFeedback.addFeedback(record);
        LOG.info("Feedback from {} to {} was processed", record.getAuthor(), employee);
    }

    private void fillMarks(CsvReader reader, Feedback feedback) {
        getCompetencies()
                .forEach(criterion -> feedback.evaluate(criterion, parseMark(reader, criterion)));
    }

    private Mark parseMark(CsvReader reader, String criterion) {
        String evaluation = readValue(reader, criterion);
        if (StringUtils.isBlank(evaluation)) {
            return Mark.MISSED;
        }
        int value = 0;
        String title = evaluation;
        if (StringUtils.contains(evaluation, "(")) {
            value = Integer.parseInt(StringUtils.substringBefore(evaluation, "(").trim());
            title = StringUtils.substringBetween(evaluation, "(", ")");
        }
        Mark mark = new Mark(value, title);
        String comment = getCommentForMark(reader, criterion);
        if (StringUtils.isNoneBlank(comment)) {
            mark.setComment(comment);
        }
        return mark;
    }

    private String getCommentForMark(CsvReader reader, String criterion) {
        try {
            int commentIndex = reader.getIndex(criterion) + 1;
            if (reader.getHeaderCount() > commentIndex &&
                    StringUtils.containsIgnoreCase(reader.getHeader(commentIndex), "comment")) {
                return reader.get(commentIndex);
            }
            return null;
        } catch (IOException e) {
            throw new IllegalStateException("Can't read comment for criterion: " + criterion, e);
        }
    }

    private String extractPersonName(CsvReader reader, String header) {
        String dirtyName = StringUtils.replace(readValue(reader, header), " - ", " ");
        return Arrays.stream(StringUtils.split(dirtyName))
                .filter(StringUtils::isNoneBlank)
                .filter(part -> !StringUtils.contains(part, "@"))
                .collect(joining(" "));
    }

    private List<String> getCompetencies() {
        String competencies = formHeader(COMPETENCIES);
        return Arrays.asList(StringUtils.splitByWholeSeparator(competencies, SEPARATOR));
    }

    private String formHeader(CsvFeedbackFormHeaders header) {
        return config.getProperty(header.name());
    }

    private String readValue(CsvReader reader, String header) {
        return SAFE_READ_VALUE.apply(reader, header);
    }

    private String joinValues(CsvReader reader, String... headers) {
        return Stream.of(headers)
                .map(header -> readValue(reader, header))
                .map(StringEscapeUtils::unescapeCsv)
                .collect(joining(""));
    }
}
