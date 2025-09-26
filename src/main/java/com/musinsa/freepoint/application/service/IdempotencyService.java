package com.musinsa.freepoint.application.service;

import com.musinsa.freepoint.application.port.in.IdempotencyPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyPort idempotencyPort;

    @Transactional
    public boolean checkAndSaveKey(String idempotencyKey) {
        if (idempotencyPort.existsByKey(idempotencyKey)) {
            return false;
        }
        idempotencyPort.saveKey(idempotencyKey);
        return true;
    }
}
