package com.example.BE.services.Maintenance;

import com.example.BE.dto.admin.response.MaintenanceHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceHistoryServiceImpl implements MaintenanceHistoryService {

    @Override
    public List<MaintenanceHistoryResponse> getMaintenanceHistory(Long maintenanceId) {
        // Nếu bạn chưa có MaintenanceHistoryModel/Repository thì tạm để trống hoặc ném lỗi rõ ràng
        throw new UnsupportedOperationException("Maintenance history is not implemented yet");
    }
}