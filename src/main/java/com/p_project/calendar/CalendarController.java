package com.p_project.calendar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/get")
    public ResponseEntity<CalendarDTO> getCalendarSummary(@RequestParam Long userId,
                                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CalendarDTO calendarDTO = calendarService.getCalendarSummary(userId, date);
        return ResponseEntity.ok(calendarDTO);
    }

    /** 해당 날짜에 쓴 글 목록 (타입, 날짜, 제목, 감정). 글 클릭 시 해당 글 상세로 이동할 때 id 사용 */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyWritingItemDTO>> getDailyWritings(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(calendarService.getDailyWritings(userId, date));
    }

}
