package com.example.project_pulse_backend.helper;

import org.junit.jupiter.api.Test;
import com.example.project_pulse_backend.exception.AppException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeHelperTest {
    @Test
    void weekRangeIncludesTheWholeMondayToSundayWeek() {
        assertNotNull(new TimeHelper());
        var range = TimeHelper.weekRange(LocalDate.of(2026, 9, 9));

        assertEquals(LocalDate.of(2026, 9, 7), range.weekStart());
        assertEquals(LocalDate.of(2026, 9, 13), range.weekEnd());
        assertEquals(range, new TimeHelper.WeekRange(range.weekStart(), range.weekEnd()));
    }

    @Test
    void elapsedWorkdaysCountsOnlySpecialDatesReachedByCalculationTime() {
        int elapsedWorkdays = TimeHelper.elapsedWorkdays(
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 10),
                List.of(LocalDate.of(2026, 9, 12)),
                List.of(LocalDate.of(2026, 9, 9)));

        assertEquals(3, elapsedWorkdays);
    }

    @Test
    void elapsedWorkdaysIncludesMakeUpDatesOnTheCalculationDate() {
        int elapsedWorkdays = TimeHelper.elapsedWorkdays(
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 14),
                LocalDate.of(2026, 9, 12),
                List.of(LocalDate.of(2026, 9, 12)),
                List.of(LocalDate.of(2026, 9, 9)));

        assertEquals(5, elapsedWorkdays);
    }

    @Test
    void workingDatesIncludeWeekdaysAndInRangeMakeUpDatesButExcludeOffDates() {
        var workingDates = TimeHelper.workingDates(
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 13),
                List.of(LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 14)),
                List.of(LocalDate.of(2026, 9, 8)));

        assertEquals(Set.of(
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 11),
                LocalDate.of(2026, 9, 12)), workingDates);
        assertTrue(!workingDates.contains(LocalDate.of(2026, 9, 8)));
        assertTrue(!workingDates.contains(LocalDate.of(2026, 9, 14)));
    }

    @Test
    void countsMakeUpDatesOnlyOnWeekendsAndOffDatesOnlyOnWeekdays() {
        var workingDates = TimeHelper.workingDates(
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 13),
                List.of(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 12)),
                List.of(LocalDate.of(2026, 9, 9), LocalDate.of(2026, 9, 13)));

        assertTrue(workingDates.contains(LocalDate.of(2026, 9, 8)));
        assertTrue(!workingDates.contains(LocalDate.of(2026, 9, 9)));
        assertTrue(workingDates.contains(LocalDate.of(2026, 9, 12)));
    }

    @Test
    void validatesDateRangesAndTaskAndPhaseBoundaries() {
        LocalDate start = LocalDate.of(2026, 9, 7);
        LocalDate end = start.plusDays(4);
        assertTrue(TimeHelper.isWeekend(LocalDate.of(2026, 9, 12)));
        assertFalse(TimeHelper.isWeekend(start));
        assertTrue(TimeHelper.workingDates(end, start, null, null).isEmpty());

        assertThrows(AppException.class, () -> TimeHelper.checkStartBeforeEnd(end, start));
        assertThrows(AppException.class, () ->
                TimeHelper.checkWorkingDateInTask(end.plusDays(1), start, end));
        assertThrows(AppException.class, () ->
                TimeHelper.checkTaskInPhase(start.minusDays(1), end, start, end));
        assertThrows(AppException.class, () ->
                TimeHelper.checkPhaseInProject(start, end.plusDays(1), start, end));
        assertDoesNotThrow(() -> TimeHelper.checkTaskInPhase(start, end, start, end));
        assertDoesNotThrow(() -> TimeHelper.checkWorkingDateInTask(null, start, end));
    }
}
