package com.example.project_pulse_backend.exception;

import com.example.project_pulse_backend.constant.AppError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private AppError appError;

}
