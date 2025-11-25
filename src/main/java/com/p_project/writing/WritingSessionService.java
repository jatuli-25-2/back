package com.p_project.writing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingSessionService {

    private final WritingSessionRepository writingSessionRepository;

    public List<WritingSessionDTO> getRecentWritingSessions(Long userId) {
        Pageable limitFive = PageRequest.of(0, 5);
        return writingSessionRepository.findRecentWritingSessions(userId, limitFive)
                .stream()
                .map(w -> new WritingSessionDTO(
                        w.getUserId(),
                        w.getTitle(),
                        w.getType().name(),
                        w.getGenre(),
                        w.getEmotion(),
                        w.getContent(),
                        w.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }


    @Transactional
    public WritingSessionEntity complete(Long id) {
        WritingSessionEntity entity = writingSessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, // 404 상태 코드
                        "Writing not found with id: " + id));

        entity.setStatus(WritingSessionEntity.WritingStatus.COMPLETE);
        return writingSessionRepository.save(entity);
    }
}
