package com.xpinjection.hr.feedback;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Mark {
    public final static Mark MISSED = new Mark(0, "Missed");

    private final int value;
    private final String title;

    private String comment;
}
