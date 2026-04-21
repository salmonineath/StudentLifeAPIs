package com.studentlife.StudentLifeAPIs.Scheduler;

import com.studentlife.StudentLifeAPIs.Repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCleanupScheduler {

    private final GroupMessageRepository groupMessageRepository;
    /**
     * Runs every day at midnight.
     * Deletes all group messages older than 5 days.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteOldMessages() {
        Instant cutoff = Instant.now().minus(5, ChronoUnit.DAYS);
        groupMessageRepository.deleteOlderThan(cutoff);
        log.info("[ChatCleanup] Deleted messages older than 5 days (cutoff: {})", cutoff);
    }

}
