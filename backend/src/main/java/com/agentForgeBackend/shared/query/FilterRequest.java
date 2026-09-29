package com.agentForgeBackend.shared.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterRequest {

    @NotBlank
    @Size(max = 64)
    private String field;

    @NotNull
    @Size(min = 1)
    private List<@Valid FilterOperationRequest> operations;
}
