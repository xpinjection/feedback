package com.xpinjection.hr.feedback;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class Feedback {
    private final String author;
    private final Map<String, Mark> marks = new HashMap<>();

    @Getter @Setter
    private String employee;
    @Setter
    private boolean anonymous = true;
    @Getter @Setter
    private String good;
    @Getter @Setter
    private String improvements;
    @Getter @Setter
    private String period;

    public void evaluate(String criterion, Mark mark) {
        marks.put(criterion, mark);
    }

    public Mark getMark(String criterion) {
        return Optional.ofNullable(marks.get(criterion)).orElse(Mark.MISSED);
    }

    public String getAuthor() {
        return (anonymous && !isSelfReview()) ? "Anonymous" : author;
    }

    private boolean isSelfReview() {
        return author.equals(employee);
    }
}
