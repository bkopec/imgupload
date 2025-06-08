export interface ImageDto {
    id: number;
    filename: string;
    publicUrl: string;
    originalFilename: string;
    contentType: string;
    size: number;
    createdAt: string;
    updatedAt: string;
  }
  
  export interface UploadStatus {
    file: File;
    // Added 'validation_error' status
    status: 'pending' | 'uploading' | 'success' | 'failed' | 'validation_error';
    progress?: number;
    message?: string; // Success URL, error message, or validation message (e.g., "File too large")
    response?: ImageDto;
  }