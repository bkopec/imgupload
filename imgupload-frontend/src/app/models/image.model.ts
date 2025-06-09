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
    status: 'pending' | 'uploading' | 'success' | 'failed' | 'validation_error';
    progress?: number;
    message?: string;
    response?: ImageDto;
  }