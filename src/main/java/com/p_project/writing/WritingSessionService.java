package com.p_project.writing;

import com.p_project.AI.AiFinalizeResponseDTO;
import com.p_project.AI.AiResponseDTO;
import com.p_project.AI.AiService;
import com.p_project.friend.FriendService;
import com.p_project.message.MessageRepository;
import com.p_project.message.MessagesEntity;
import com.p_project.message.feedback.FeedbackRequestDTO;
import com.p_project.message.feedback.FeedbackResponDTO;
import com.p_project.message.finalize.FinalizeRequestDTO;
import com.p_project.message.finalize.FinalizeResponseDTO;
import com.p_project.AI.TitleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WritingSessionService {

    private final WritingSessionRepository writingSessionRepository;
    private final MessageRepository messageRepository;
    private final AiService aiService;
    private final FriendService friendService;

    private final int INITIAL_QUESTIONS = 5;

    // ---------------------------------------
    // 1) 글쓰기 시작
    // ---------------------------------------
    public StartResponseDTO startWriting(StartRequestDTO request) {

        // DB에 세션 생성
        WritingSessionEntity session = WritingSessionEntity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .status(WritingSessionEntity.WritingStatus.DRAFT)
                .build();

        writingSessionRepository.save(session);

        // AI → 첫 질문 요청 (messages 없음)
        String mode = session.getType().name();
        String first = aiService.getFirstQuestion(mode);

        // AI 질문 DB 저장
        messageRepository.save(
                MessagesEntity.builder()
                        .sessionId(session.getId())
                        .role(MessagesEntity.MessageRole.AI)
                        .content(first)
                        .build()
        );

        return StartResponseDTO.builder()
                .sessionId(session.getId())
                .question(first)
                .build();
    }

    // ---------------------------------------
    // 2) 답변 처리 + 다음 질문 생성
    // ---------------------------------------
    public AnswerResponseDTO submitAnswer(AnswerRequestDTO request) {

        WritingSessionEntity session = writingSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 사용자 답변 저장
        messageRepository.save(
                MessagesEntity.builder()
                        .sessionId(request.getSessionId())
                        .role(MessagesEntity.MessageRole.USER)
                        .content(request.getAnswer())
                        .build()
        );

        // 모든 메시지 로드
        List<MessagesEntity> history =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(request.getSessionId());

        // 현재 질문 개수 = AI 메시지 개수
        int aiCount = (int) history.stream()
                .filter(m -> m.getRole() == MessagesEntity.MessageRole.AI)
                .count();

        int totalQuestions = INITIAL_QUESTIONS + session.getExtraQuestions();

        // 마지막 질문이면 finalize 안내
        if (aiCount >= totalQuestions) {
            return AnswerResponseDTO.builder()
                    .nextQuestion(null)
                    .finalize(true)
                    .currentIndex(aiCount)
                    .totalQuestions(totalQuestions)
                    .build();
        }

        // 다음 질문 생성
        AiResponseDTO ai = aiService.generateNextQuestion(
                session.getType().name(),
                history
        );

        // AI 질문 저장
        messageRepository.save(
                MessagesEntity.builder()
                        .sessionId(session.getId())
                        .role(MessagesEntity.MessageRole.AI)
                        .content(ai.getNextQuestion())
                        .build()
        );

        return AnswerResponseDTO.builder()
                .nextQuestion(ai.getNextQuestion())
                .emotion(ai.getEmotion())
                .finalize(false)
                .currentIndex(aiCount + 1)
                .totalQuestions(totalQuestions)
                .build();
    }

    // ---------------------------------------
    // 3) 완성 처리 (finalize)
    // ---------------------------------------
    public FinalizeResponseDTO finalizeWriting(FinalizeRequestDTO request) {

        WritingSessionEntity session = writingSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 모든 메시지 로드
        List<MessagesEntity> history =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(request.getSessionId());

        AiFinalizeResponseDTO aiResult =
                aiService.generateFinalText(session.getType().name(), history);

        // DB 저장
        session.setContent(aiResult.getContent());
        session.setEmotion(aiResult.getEmotion());
        session.setRecommendTitle(aiResult.getRecommendTitle());
        session.setRecommendGenre(aiResult.getRecommendGenre());
        session.setStatus(WritingSessionEntity.WritingStatus.COMPLETE);

        writingSessionRepository.save(session);

        // 같은 감정 count 조회
        int emotionCount = writingSessionRepository.countByEmotionAndCreatedAt(
                aiResult.getEmotion(),
                session.getCreatedAt().toLocalDate()
        );

        return FinalizeResponseDTO.builder()
                .sessionId(session.getId())
                .content(aiResult.getContent())
                .title(session.getTitle())
                .emotion(aiResult.getEmotion())
                .emotionCount(emotionCount-1)
                .recommendTitle(aiResult.getRecommendTitle())
                .recommendGenre(aiResult.getRecommendGenre())
                .build();
    }

    // ---------------------------------------
    // 4) 추가 질문 요청
    // ---------------------------------------
    // ---------------------------------------
    // 제목: AI 추천 3개 조회 / 사용자가 고른 제목 저장
    // ---------------------------------------
    public TitleResponseDTO handleTitle(TitleRequestDTO request) {

        WritingSessionEntity session = writingSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        String chosenTitle = request.getTitle();
        if (chosenTitle != null && !chosenTitle.isBlank()) {
            // 사용자가 고른 제목(추천 중 하나 또는 직접 입력) → 그대로 글 제목으로 저장
            String toSave = chosenTitle.length() > 100 ? chosenTitle.substring(0, 100) : chosenTitle.trim();
            session.setTitle(toSave);
            writingSessionRepository.save(session);
            return TitleResponseDTO.builder().title(toSave).build();
        }

        // 제목 없이 요청 → AI에게 제목 3개 받아서 반환
        String finalText = session.getContent();
        String dominantEmotion = session.getEmotion();
        if (finalText == null || finalText.isBlank()) {
            throw new RuntimeException("Session has no content. Finalize first.");
        }

        TitleResponse ai = aiService.callTitleApi(
                session.getType().name(),
                finalText,
                dominantEmotion,
                null,
                null,
                null
        );

        return TitleResponseDTO.builder().titles(ai.getTitles()).build();
    }

    // ---------------------------------------
    // 4) 추가 질문 요청
    // ---------------------------------------
    public FeedbackResponDTO handleFeedback(FeedbackRequestDTO request) {

        WritingSessionEntity session = writingSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (request.isSatisfied()) {
            session.setStatus(WritingSessionEntity.WritingStatus.COMPLETE);
            writingSessionRepository.save(session);
            return FeedbackResponDTO.builder().done(true).build();
        }

        session.setExtraQuestions(request.getAddN());
        writingSessionRepository.save(session);

        List<MessagesEntity> history =
                messageRepository.findBySessionIdOrderByCreatedAtAsc(request.getSessionId());

        AiResponseDTO ai = aiService.generateNextQuestion(
                session.getType().name(),
                history
        );

        messageRepository.save(
                MessagesEntity.builder()
                        .sessionId(session.getId())
                        .role(MessagesEntity.MessageRole.AI)
                        .content(ai.getNextQuestion())
                        .build()
        );

        return FeedbackResponDTO.builder()
                .sessionId(session.getId())
                .done(false)
                .question(ai.getNextQuestion())
                .build();
    }

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

    /**
     * 글 한 건 상세 조회 (캘린더에서 글 클릭 시 해당 글로 이동한 화면용). 본인 글 또는 친구 글 조회 가능.
     */
    public WritingDetailDTO getWritingDetail(Long id, Long userId) {
        WritingSessionEntity entity = writingSessionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Writing not found with id: " + id));
        boolean isOwner = entity.getUserId().equals(userId);
        boolean isFriend = friendService.areMutualFriends(userId, entity.getUserId());
        if (!isOwner && !isFriend) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to view this writing");
        }
        return WritingDetailDTO.builder()
                .id(entity.getId())
                .type(entity.getType().name())
                .createdAt(entity.getCreatedAt())
                .date(entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : null)
                .title(entity.getTitle())
                .content(entity.getContent())
                .emotion(entity.getEmotion())
                .recommendTitle(entity.getRecommendTitle())
                .recommendGenre(entity.getRecommendGenre())
                .build();
    }
}
