package com.easypan.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadFileDto {
    private String downloadCode;
    private String fileId;
    private String fileName;
    private String filePath;

}
