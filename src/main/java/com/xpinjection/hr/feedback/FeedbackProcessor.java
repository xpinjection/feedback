package com.xpinjection.hr.feedback;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FeedbackProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackProcessor.class);

    public static void main(String[] args) {
        String format = args[0];
        File feedbackFile = new File("feedback.csv");
        LOG.info("Start processing {} review feedbacks from {}", format, feedbackFile);

        File anonymousRequests = new File("anonymous-requests.txt");
        Set<FeedbackSummary> feedback = new CsvFeedbackForm(feedbackFile, format)
                .withAnonymousFeedbackFor(readEmployees(anonymousRequests))
                .aggregateFeedbackSummary();

        LOG.info("All feedbacks was processed. Start generating summaries.");

        File summariesDir = new File("summaries-" + format);
        String templateFileName = "feedback-" + format + ".html";
        new FeedbackSummaryReports(summariesDir, templateFileName).generate(feedback);

        LOG.info("Summaries was generated for all employees.");
    }

    private static List<String> readEmployees(File anonymousRequests) {
        if (!anonymousRequests.exists()) {
            return Collections.emptyList();
        }
        try {
            LOG.info("Read anonymous requests from {}", anonymousRequests);
            return FileUtils.readLines(anonymousRequests, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("Can't read employees from file: " + anonymousRequests, e);
        }
    }
}
