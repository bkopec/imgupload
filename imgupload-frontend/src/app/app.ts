import { Component, signal, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpEventType } from '@angular/common/http';
import { ImageService } from './services/image.service';
import { ImageDto, UploadStatus } from './models/image.model';
import { tap, catchError, finalize } from 'rxjs/operators';
import { throwError } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  imports: [CommonModule]
})
export class AppComponent {
  title = 'Image Host';

  selectedFiles = signal<File[]>([]);
  uploads = signal<UploadStatus[]>([]);
  fileInput: HTMLInputElement | null = null;

  readonly MAX_FILE_SIZE_MB = 10; // 10 MB
  readonly MAX_FILE_SIZE_BYTES = this.MAX_FILE_SIZE_MB * 1024 * 1024;

  readonly ALLOWED_MIME_TYPES = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/webp',
    'image/bmp',
    'image/tiff'
  ];

  constructor(private imageService: ImageService, private cdr: ChangeDetectorRef) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    this.fileInput = input;

    if (!input.files || input.files.length === 0) {
      this.selectedFiles.set([]);
      this.uploads.update(currentUploads =>
        currentUploads.filter(u => u.status === 'uploading' || u.status === 'success' || u.status === 'failed')
      );
      this.fileInput.value = '';
      return;
    }

    const tempValidFiles: File[] = [];
    const tempInvalidUploadStatuses: UploadStatus[] = [];

    this.uploads.update(currentUploads =>
      currentUploads.filter(u => u.status === 'uploading' || u.status === 'success' || u.status === 'failed')
    );


    Array.from(input.files).forEach(file => {
      let isValid = true;
      let errorMessage = '';

      if (file.size > this.MAX_FILE_SIZE_BYTES) {
        isValid = false;
        errorMessage += `File size exceeds ${this.MAX_FILE_SIZE_MB}MB. `;
      }

      if (!this.ALLOWED_MIME_TYPES.includes(file.type)) {
        isValid = false;
        errorMessage += `Invalid file type: ${file.type}. Only images are allowed. `;
      }

      if (isValid) {
        tempValidFiles.push(file);
      } else {
        tempInvalidUploadStatuses.push({
          file: file,
          status: 'validation_error',
          message: errorMessage
        });
      }
    });

    this.selectedFiles.set(tempValidFiles);
    this.uploads.update(currentUploads => [...tempInvalidUploadStatuses, ...currentUploads]);

    if (tempValidFiles.length === 0 && input.files.length > 0) {
        this.fileInput.value = '';
    }
  }

  onUpload(): void {
    const filesToProcess = this.selectedFiles();

    if (filesToProcess.length === 0) {
      alert('No valid files selected for upload. Please select files that are images and under ' + this.MAX_FILE_SIZE_MB + 'MB.');
      return;
    }

    if (this.fileInput) {
      this.fileInput.value = '';
    }
    this.selectedFiles.set([]);

    const newUploadStatuses: UploadStatus[] = filesToProcess.map(file => ({
      file: file,
      status: 'pending',
      message: 'Waiting...'
    }));
    this.uploads.update(currentUploads => [...newUploadStatuses, ...currentUploads]);

    filesToProcess.forEach(file => {
      const uploadStatusIndex = this.uploads().findIndex(u => u.file === file && u.status === 'pending');
      if (uploadStatusIndex === -1) return;

      this.uploads.update(currentUploads => {
        const updated = [...currentUploads];
        updated[uploadStatusIndex] = { ...updated[uploadStatusIndex], status: 'uploading', message: 'Uploading...' };
        return updated;
      });

      this.imageService.uploadImage(file).pipe(
        tap(event => {
          if (event.type === HttpEventType.UploadProgress) {
            const percentDone = Math.round(100 * event.loaded / (event.total || 1));
            this.uploads.update(currentUploads => {
              const updated = [...currentUploads];
              updated[uploadStatusIndex] = { ...updated[uploadStatusIndex], progress: percentDone };
              return updated;
            });
          } else if (event.type === HttpEventType.Response) {
            this.uploads.update(currentUploads => {
              const updated = [...currentUploads];
              updated[uploadStatusIndex] = {
                ...updated[uploadStatusIndex],
                status: 'success',
                message: 'Uploaded successfully!',
                response: event.body as ImageDto
              };
              return updated;
            });
            this.cdr.detectChanges();
          }
        }),
        catchError(error => {
          this.uploads.update(currentUploads => {
            const updated = [...currentUploads];
            const errorMessage = error.error?.message || 'Unknown error during upload.';
            updated[uploadStatusIndex] = {
              ...updated[uploadStatusIndex],
              status: 'failed',
              message: `Failed: ${errorMessage}`
            };
            return updated;
          });
          this.cdr.detectChanges();
          return throwError(() => error);
        })
      ).subscribe();
    });
  }
}