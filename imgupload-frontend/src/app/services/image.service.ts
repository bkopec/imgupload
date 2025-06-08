import { Injectable } from '@angular/core';
import { HttpClient, HttpEventType, HttpEvent } from '@angular/common/http';
import { Observable, catchError, map, tap } from 'rxjs';
import { ImageDto } from '../models/image.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageService {
  private apiUrl = `${environment.backendUrl}/api/images`;

  constructor(private http: HttpClient) { }

  uploadImage(file: File): Observable<HttpEvent<ImageDto>> {
    const formData = new FormData();
    formData.append('file', file, file.name);

    return this.http.post<ImageDto>(`${this.apiUrl}/upload`, formData, {
      reportProgress: true,
      observe: 'events'
    }).pipe(
      tap(event => {
        if (event.type === HttpEventType.UploadProgress) {
          const percentDone = Math.round(100 * event.loaded / (event.total || 1));
          console.log(`File "${file.name}" is ${percentDone}% uploaded.`);
        } else if (event.type === HttpEventType.Response) {
          console.log(`File "${file.name}" uploaded successfully.`, event.body);
        }
      }),
      catchError(error => {
        console.error(`Upload failed for "${file.name}":`, error);
        throw error;
      })
    );
  }
}