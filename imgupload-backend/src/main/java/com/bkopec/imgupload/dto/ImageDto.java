package com.bkopec.imgupload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private Long id;
    private String filename;
    private String publicUrl;
    private String originalFilename;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}