package com.example.project_pulse_backend.mapper;

import com.example.project_pulse_backend.dto.response.OverloadedDateResponse;
import com.example.project_pulse_backend.entity.AllocationOverloadedDate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AllocationOverloadedDateMapper {
    OverloadedDateResponse toOverloadedDateResponse(AllocationOverloadedDate date);
}
