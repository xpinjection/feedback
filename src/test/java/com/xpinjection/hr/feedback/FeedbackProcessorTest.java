package com.xpinjection.hr.feedback;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeedbackProcessorTest {
    private Set<FeedbackSummary> feedback;
    private File summariesDir;

    @Before
    public void init() {
        File feedbackFile = new File("src/test/resources/feedback.csv");
        feedback = new CsvFeedbackForm(feedbackFile, "test").aggregateFeedbackSummary();
    }

    @After
    public void cleanup() throws IOException {
        if (summariesDir != null) {
            FileUtils.deleteDirectory(summariesDir);
        }
    }

    @Test
    public void summaryIsAggregatedWithAverageMarks() {
        assertThat(feedback.size(), is(2));
        FeedbackSummary alimenkou = feedback.stream()
                .filter(summary -> summary.getEmployee().equals("Mikalai Alimenkou"))
                .findAny()
                .orElseThrow(() -> new AssertionError("No feedback for Mikalai Alimenkou found"));

        assertThat(alimenkou.getEmployee(), equalTo("Mikalai Alimenkou"));
        assertThat(alimenkou.getFeedbackCount(), is(3));
        assertThat(alimenkou.getAverage("Position vs Competency"), is(0.0));
        assertThat(alimenkou.getAverage("Communication in the team"), is(2.0));
    }

    @Test
    public void summaryReportIsGeneratedForAllEmployees() throws IOException {
        String templateFileName = "src/test/resources/plain-template.txt";
        summariesDir = Files.createTempDirectory("feedback").toFile();
        new FeedbackSummaryReports(summariesDir, templateFileName).generate(feedback);

        File alimenkou = new File(summariesDir, "Mikalai Alimenkou.html");
        String actualSummary = FileUtils.readFileToString(alimenkou, "UTF-8");
        File expectedReportFile = new File("src/test/resources/expected-summary-alimenkou.txt");
        String expectedSummary = FileUtils.readFileToString(expectedReportFile, "UTF-8");
        assertThat(actualSummary, is(equalTo(expectedSummary)));
    }
}
