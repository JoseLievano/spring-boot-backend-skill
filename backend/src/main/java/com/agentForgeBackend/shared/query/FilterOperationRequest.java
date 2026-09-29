package com.agentForgeBackend.shared.query;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterOperationRequest {

    @NotNull
    private FilterOperator operator;

    private Object value;
}
