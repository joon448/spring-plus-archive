package org.example.expert.domain.log.service;

import java.time.LocalDateTime;

import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {
	private final LogRepository logRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void logSaveManager(Long userId, Long managerId, Long todoId){
		try{
			logRepository.save(new Log(userId, managerId, todoId));
		} catch(Exception e){
			log.warn("Manager Save Log Failed - User ID: {}, Request Time: {}, Request managerId: {}, Request todoId: {}",
				userId, LocalDateTime.now(), managerId, todoId);
		}
	}
}
