package com.xpinjection.hr.feedback;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static com.xpinjection.hr.feedback.Feedback.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class FeedbackProcessorTest {
    private File summariesDir;
    private CsvFeedbackForm feedbackForm;

    @Before
    public void init() {
        File feedbackFile = new File("src/test/resources/feedback.csv");
        feedbackForm = new CsvFeedbackForm(feedbackFile, "test");
    }

    @After
    public void cleanup() throws IOException {
        if (summariesDir != null) {
            FileUtils.deleteDirectory(summariesDir);
        }
    }

    @Test
    public void summaryIsAggregatedWithAverageMarks() {
        Set<FeedbackSummary> feedback = feedbackForm.aggregateFeedbackSummary();
        assertThat(feedback.size(), is(2));
        FeedbackSummary alimenkou = getFeedbackFor(feedback, "Mikalai Alimenkou");

        assertThat(alimenkou.getEmployee(), equalTo("Mikalai Alimenkou"));
        assertThat(alimenkou.getFeedbackCount(), is(3));
        assertThat(alimenkou.getAverage("Position vs Competency"), is(0.0));
        assertThat(alimenkou.getAverage("Communication in the team"), is(2.0));
    }

    @Test
    public void ifEmployeeWantsToGetAnonymousFeedbackAllAuthorsAreMasked() {
        Set<FeedbackSummary> feedback = feedbackForm
                .withAnonymousFeedbackFor(singletonList("Mikalai Alimenkou"))
                .aggregateFeedbackSummary();

        FeedbackSummary alimenkou = getFeedbackFor(feedback, "Mikalai Alimenkou");
        Set<String> authors = alimenkou.getFeedbacks().stream()
                .map(Feedback::getAuthor)
                .collect(toSet());

        assertThat(authors, is(equalTo(new HashSet<>(asList("Mikalai Alimenkou", ANONYMOUS)))));
    }

    @Test
    public void summaryReportIsGeneratedForAllEmployees() throws IOException {
        Set<FeedbackSummary> feedback = feedbackForm.aggregateFeedbackSummary();

        String templateFileName = "src/test/resources/plain-template.txt";
        summariesDir = Files.createTempDirectory("feedback").toFile();
        new FeedbackSummaryReports(summariesDir, templateFileName).generate(feedback);

        File alimenkou = new File(summariesDir, "Mikalai Alimenkou.html");
        String actualSummary = FileUtils.readFileToString(alimenkou, "UTF-8");
        File expectedReportFile = new File("src/test/resources/expected-summary-alimenkou.txt");
        String expectedSummary = FileUtils.readFileToString(expectedReportFile, "UTF-8");
        assertThat(actualSummary, is(equalTo(expectedSummary)));
    }

    private FeedbackSummary getFeedbackFor(Set<FeedbackSummary> feedback, String employee) {
        return feedback.stream()
                .filter(summary -> summary.getEmployee().equals(employee))
                .findAny()
                .orElseThrow(() -> new AssertionError("No feedback found for " + employee));
    }
}
