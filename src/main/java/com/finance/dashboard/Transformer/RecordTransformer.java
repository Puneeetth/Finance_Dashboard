package com.finance.dashboard.Transformer;

import com.finance.dashboard.dto.request.RecordRequest;
import com.finance.dashboard.dto.response.RecordResponse;
import com.finance.dashboard.models.FinancialRecord;
import com.finance.dashboard.models.User;
import org.springframework.stereotype.Component;

@Component
public class RecordTransformer {

    public FinancialRecord toEntity(RecordRequest request, User user) {
        if (request == null) {
            return null;
        }
        return FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .description(request.getDescription())
                .user(user)
                .build();
    }

    public RecordResponse toResponse(FinancialRecord record) {
        if (record == null) {
            return null;
        }
        return RecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .description(record.getDescription())
                .createdAt(record.getCreatedAt())
                .build();
    }
}
