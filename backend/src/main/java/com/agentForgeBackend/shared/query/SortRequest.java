package com.agentForgeBackend.shared.query;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortRequest {

    @NotBlank
    @Size(max = 64)
    private String field;

    @NotNull
    private SortDirection direction;
}
