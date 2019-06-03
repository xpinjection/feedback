package com.xpinjection.hr.feedback;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class FeedbackSummaryReports {
    private static final Logger LOG = LoggerFactory.getLogger(FeedbackSummaryReports.class);

    private final File summariesDir;
    private final FreemarkerFeedbackSummaryTemplate template;

    public FeedbackSummaryReports(String format) {
        summariesDir = new File("summaries-" + format);
        try {
            prepareReportsDirectory();
            template = FreemarkerFeedbackSummaryTemplate.fromFile("feedback-" + format + ".html");
        } catch (IOException e) {
            throw new IllegalStateException("Can't create or clean summaries directory", e);
        }
    }

    public void generate(Set<FeedbackSummary> feedback) {
        feedback.forEach(summary -> template.generateSummaryReport(summariesDir, summary));
    }

    private void prepareReportsDirectory() throws IOException {
        if (summariesDir.exists()) {
            LOG.info("Try to clean summaries directory: {}", summariesDir);
            FileUtils.cleanDirectory(summariesDir);
        } else {
            LOG.info("Try to create summaries directory: {}", summariesDir);
            Validate.isTrue(summariesDir.mkdir(), "Directory was not created");
        }
    }
}
