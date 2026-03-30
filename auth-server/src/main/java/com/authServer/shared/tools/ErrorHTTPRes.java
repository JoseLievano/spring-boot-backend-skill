package com.authServer.shared.tools;

import lombok.*;

import java.time.LocalDateTime;

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
