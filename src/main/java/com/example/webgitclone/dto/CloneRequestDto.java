package com.example.webgitclone.dto;

import lombok.Data;

@Data
public class CloneRequestDto {
    private String branch;
    private String targetDir;
    private String fileName;
}
