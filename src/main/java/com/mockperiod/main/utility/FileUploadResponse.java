package com.mockperiod.main.utility;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
	
	 private String fileName;
	    private String fileDownloadUri;
	    private String fileType;
	    private long size;
	    private LocalDateTime uploadTime;

}
