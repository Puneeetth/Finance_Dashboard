package com.finance.dashboard.service;

import com.finance.dashboard.dto.response.DashboardSummaryResponse;
import com.finance.dashboard.enums.RecordType;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.models.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void getDashboardSummary_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recordRepository.calculateTotalIncome(1L)).thenReturn(new BigDecimal("5000.00"));
        when(recordRepository.calculateTotalExpense(1L)).thenReturn(new BigDecimal("2000.00"));
        
        when(recordRepository.calculateCategoryTotals(1L, RecordType.INCOME))
                .thenReturn(Collections.singletonList(new Object[]{"Salary", new BigDecimal("5000.00")}));
        when(recordRepository.calculateCategoryTotals(1L, RecordType.EXPENSE))
                .thenReturn(Collections.singletonList(new Object[]{"Rent", new BigDecimal("2000.00")}));

        // Act
        DashboardSummaryResponse response = dashboardService.getDashboardSummary("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("2000.00"), response.getTotalExpenses());
        assertEquals(new BigDecimal("3000.00"), response.getNetBalance());
        assertEquals(2, response.getCategoryTotals().size());
        assertEquals(new BigDecimal("5000.00"), response.getCategoryTotals().get("INCOME - Salary"));
        assertEquals(new BigDecimal("2000.00"), response.getCategoryTotals().get("EXPENSE - Rent"));
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(recordRepository, times(1)).calculateTotalIncome(1L);
        verify(recordRepository, times(1)).calculateTotalExpense(1L);
    }

    @Test
    void getDashboardSummary_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("wronguser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> dashboardService.getDashboardSummary("wronguser"));
        verify(recordRepository, never()).calculateTotalIncome(any());
    }

    @Test
    void getDashboardSummary_NullTotals_ReturnsZeroes() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(recordRepository.calculateTotalIncome(1L)).thenReturn(null);
        when(recordRepository.calculateTotalExpense(1L)).thenReturn(null);
        when(recordRepository.calculateCategoryTotals(anyLong(), any())).thenReturn(Collections.emptyList());

        // Act
        DashboardSummaryResponse response = dashboardService.getDashboardSummary("testuser");

        // Assert
        assertEquals(BigDecimal.ZERO, response.getTotalIncome());
        assertEquals(BigDecimal.ZERO, response.getTotalExpenses());
        assertEquals(BigDecimal.ZERO, response.getNetBalance());
    }
}
