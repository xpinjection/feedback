package com.xpinjection.hr.feedback;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FreemarkerFeedbackSummaryTemplate {
    private static final Logger LOG = LoggerFactory.getLogger(FreemarkerFeedbackSummaryTemplate.class);

    private final Template template;

    public static FreemarkerFeedbackSummaryTemplate fromFile(String name) {
        FreeMarkerConfigurationFactory factory = new FreeMarkerConfigurationFactory();
        factory.setDefaultEncoding("UTF-8");
        Properties settings = new Properties();
        settings.setProperty("number_format", "0.##");
        settings.setProperty("default_encoding", "UTF-8");
        settings.setProperty("url_escaping_charset", "UTF-8");
        factory.setFreemarkerSettings(settings);
        try {
            Configuration config = factory.createConfiguration();
            Template template = config.getTemplate(name, Locale.US, "UTF-8");
            return new FreemarkerFeedbackSummaryTemplate(template);
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Can't read template " + name, e);
        }
    }

    public void generateSummaryReport(File summariesDir, FeedbackSummary feedback) {
        String employee = feedback.getEmployee();
        LOG.info("Generate summary report for employee: {}", employee);
        Map<String, Object> params = new HashMap<>();
        params.put("employee", employee);
        params.put("feedback", feedback);
        try {
            String summary = FreeMarkerTemplateUtils.processTemplateIntoString(template, params);
            File summaryFile = new File(summariesDir, employee + ".html");
            FileUtils.writeStringToFile(summaryFile, summary, "UTF-8");
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Can't write summary for " + employee, e);
        }
    }
}
