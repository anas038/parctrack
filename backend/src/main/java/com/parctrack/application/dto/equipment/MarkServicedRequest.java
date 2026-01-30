package com.parctrack.application.dto.equipment;

import com.parctrack.domain.equipment.ReasonCode;

public record MarkServicedRequest(
        ReasonCode reasonCode
) {}
