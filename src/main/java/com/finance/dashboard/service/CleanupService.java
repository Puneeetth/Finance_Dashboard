package com.finance.dashboard.service;

import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void purgeOldDeletedRecords() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        
        log.info("Starting scheduled cleanup for records deleted before {}", thresholdDate);
        
        try {
            recordRepository.purgeOldRecords(thresholdDate);
            userRepository.purgeOldUsers(thresholdDate);
            log.info("Cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage());
        }
    }
}
