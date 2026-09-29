package com.agentForgeBackend.shared.tools;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorHTTPRes {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

}
