package org.example.mirimilibe.ranking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RankingSchedulerService {

    private final RankingService rankingService;

    @Scheduled(cron = "0 0 * * * ?") // 매시간 정각
    public void updateHotQuestions() {
        log.info("HOT 질문 업데이트 시작");
        try {
            rankingService.updateHotQuestions();
            log.info("HOT 질문 업데이트 완료");
        } catch (Exception e) {
            log.error("HOT 질문 업데이트 중 오류 발생", e);
        }
    }

    @Scheduled(cron = "0 */10 * * * ?") // 10분마다
    public void updateAllBestAnswers() {
        log.info("전체 베스트 답변 업데이트 시작");
        try {
            // 모든 질문의 베스트 답변을 재계산하는 것은 부하가 클 수 있으므로
            // 최근 활동이 있었던 질문들만 업데이트하는 방식으로 최적화 가능
            log.info("전체 베스트 답변 업데이트는 개별 답변/좋아요 시에 실시간으로 처리됩니다.");
        } catch (Exception e) {
            log.error("베스트 답변 업데이트 중 오류 발생", e);
        }
    }
}