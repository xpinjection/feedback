package com.xpinjection.hr.feedback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

public class FeedbackProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackProcessor.class);

    public static void main(String[] args) {
        String format = args[0];
        File feedbackFile = new File("feedback.csv");
        LOG.info("Start processing {} review feedbacks from {}", format, feedbackFile);

        Set<FeedbackSummary> feedback = new CsvFeedbackForm(feedbackFile, format).aggregateFeedbackSummary();

        LOG.info("All feedbacks was processed. Start generating summaries.");

        File summariesDir = new File("summaries-" + format);
        String templateFileName = "feedback-" + format + ".html";
        new FeedbackSummaryReports(summariesDir, templateFileName).generate(feedback);

        LOG.info("Summaries was generated for all employees.");
    }
}
