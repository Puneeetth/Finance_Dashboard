package com.finance.dashboard.repository;

import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.models.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {
    
    List<FinancialRecord> findByUserIdOrderByDateDesc(Long userId);
    
    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = 'INCOME'")
    BigDecimal calculateTotalIncome(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = 'EXPENSE'")
    BigDecimal calculateTotalExpense(@Param("userId") Long userId);
    
    @Query("SELECT f.category, COALESCE(SUM(f.amount), 0) FROM FinancialRecord f WHERE f.user.id = :userId AND f.type = :type GROUP BY f.category")
    List<Object[]> calculateCategoryTotals(@Param("userId") Long userId, @Param("type") RecordType type);
}
