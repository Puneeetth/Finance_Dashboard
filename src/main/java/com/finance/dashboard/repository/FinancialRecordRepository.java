package com.finance.dashboard.repository;

import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.models.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {
    
    Page<FinancialRecord> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);
    
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = 'INCOME'")
    BigDecimal calculateTotalIncome(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = 'EXPENSE'")
    BigDecimal calculateTotalExpense(@Param("userId") Long userId);
    
    @Query("SELECT f.category, COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = :type GROUP BY f.category")
    List<Object[]> calculateCategoryTotals(@Param("userId") Long userId, @Param("type") RecordType type);

    @Query(value = "DELETE FROM financial_records WHERE deleted = true AND deleted_at < :date", nativeQuery = true)
    void purgeOldRecords(@Param("date") LocalDateTime date);
}
