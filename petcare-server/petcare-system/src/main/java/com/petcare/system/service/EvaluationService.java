package com.petcare.system.service;

import com.petcare.system.dto.EvaluationCreateDTO;
import com.petcare.system.dto.EvaluationDTO;
import com.petcare.system.dto.EvaluationReplyDTO;

public interface EvaluationService {

    EvaluationDTO createEvaluation(EvaluationCreateDTO dto, Long userId);

    EvaluationDTO replyEvaluation(Long evaluationId, EvaluationReplyDTO dto, Long doctorId);
}
