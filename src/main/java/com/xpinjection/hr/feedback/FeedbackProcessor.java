package com.xpinjection.hr.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Set;

public class FeedbackProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackProcessor.class);

    private static final String CSV_FEEDBACK_FILE_NAME = "feedback.csv";

    public static void main(String[] args) {
        String format = args[0];
        LOG.info("Start processing {} review feedbacks from {}", format, CSV_FEEDBACK_FILE_NAME);

        Set<FeedbackSummary> feedback = new CsvFeedbackForm(CSV_FEEDBACK_FILE_NAME, format)
                .aggregateFeedbackSummary();

        LOG.info("All feedbacks was processed. Start generating summaries.");

        new FeedbackSummaryReports(format).generate(feedback);

        LOG.info("Summaries was generated for all employees.");
    }
}
