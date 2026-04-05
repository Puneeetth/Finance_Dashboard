package com.finance.dashboard.service;

import com.finance.dashboard.Transformer.RecordTransformer;
import com.finance.dashboard.dto.request.RecordRequest;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.enums.Role;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.exception.UnauthorizedException;
import com.finance.dashboard.models.FinancialRecord;
import com.finance.dashboard.models.User;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final RecordTransformer recordTransformer;

    @CacheEvict(value = "dashboardSummary", key = "#username")
    public RecordResponse createRecord(RecordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.VIEWER) {
            throw new UnauthorizedException("Viewers cannot create records");
        }

        FinancialRecord record = recordTransformer.toEntity(request, user);
        FinancialRecord savedRecord = recordRepository.save(record);
        
        return recordTransformer.toResponse(savedRecord);
    }

    public List<RecordResponse> getAllRecordsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return recordRepository.findByUserIdOrderByDateDesc(user.getId())
                .stream()
                .map(recordTransformer::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "dashboardSummary", key = "#username")
    public RecordResponse updateRecord(Long id, RecordRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        // Admins can update any record, but others can only update their own
        if (user.getRole() != Role.ADMIN && !record.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to update this record");
        }
        if (user.getRole() == Role.VIEWER) {
            throw new UnauthorizedException("Viewers cannot update records");
        }

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setDescription(request.getDescription());

        FinancialRecord updatedRecord = recordRepository.save(record);
        return recordTransformer.toResponse(updatedRecord);
    }

    @CacheEvict(value = "dashboardSummary", key = "#username")
    public void deleteRecord(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found"));

        if (user.getRole() != Role.ADMIN && !record.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to delete this record");
        }
        if (user.getRole() == Role.VIEWER || user.getRole() == Role.ANALYST) {
            throw new UnauthorizedException("Only Admins can delete records");
        }

        recordRepository.delete(record);
    }
}
