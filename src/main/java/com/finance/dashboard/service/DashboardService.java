package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.models.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    public DashboardSummaryResponse getDashboardSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long userId = user.getId();

        BigDecimal totalIncome = recordRepository.calculateTotalIncome(userId);
        BigDecimal totalExpense = recordRepository.calculateTotalExpense(userId);
        
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;
        
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // Calculate Category Totals
        Map<String, BigDecimal> categoryTotals = new HashMap<>();
        
        List<Object[]> incomeCategories = recordRepository.calculateCategoryTotals(userId, RecordType.INCOME);
        for (Object[] result : incomeCategories) {
            categoryTotals.put("INCOME - " + result[0], (BigDecimal) result[1]);
        }

        List<Object[]> expenseCategories = recordRepository.calculateCategoryTotals(userId, RecordType.EXPENSE);
        for (Object[] result : expenseCategories) {
            categoryTotals.put("EXPENSE - " + result[0], (BigDecimal) result[1]);
        }

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpense)
                .netBalance(netBalance)
                .categoryTotals(categoryTotals)
                .build();
    }
}
