package com.xpinjection.hr.feedback;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "employee")
public class FeedbackSummary {
    private final String employee;
    private final List<Feedback> feedbacks = new ArrayList<>();

    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }

    public double getAverage(String criterion) {
        return feedbacks.stream()
                .mapToInt(feedback -> feedback.getMark(criterion).getValue())
                .average()
                .orElse(0);
    }

    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }

    public int getFeedbackCount() {
        return feedbacks.size();
    }
}
