package com.example.project_pulse_backend.constant;

import lombok.Getter;

@Getter
public enum ScheduleStatus {
    NO_DATA("CHƯA_CÓ_DỮ_LIỆU"),
    FAST("NHANH"),
    SLOW("CHẬM"),
    ON_SCHEDULE("ĐÚNG_TIẾN_ĐỘ");

    private final String label;

    ScheduleStatus(String label) {
        this.label = label;
    }

}
