import { Injectable } from '@angular/core';
import { environment } from '../enviroments/enviroment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { AuthService } from './auth/auth.service';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getUserInfo(userId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${userId}`);
  }

  getUserProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/profile`);
  }

  editProfile(userData: any): Observable<any> {
    console.log('Updating profile with data:', userData);

    return this.http.put(`${this.apiUrl}/`, userData);
  }

  getUserImage(userId: string): Observable<string> {
    return this.http
      .get(`${this.apiUrl}/${userId}/image`, {
        responseType: 'blob',
        // Ensure we don't try to parse the response as JSON
        observe: 'response',
      })
      .pipe(
        switchMap((response) => {
          // Get content type from headers if available
          const contentType =
            response.headers.get('Content-Type') || 'image/jpeg';
          const blob = new Blob([response.body as BlobPart], {
            type: contentType,
          });

          console.log(
            'Blob recibido:',
            blob,
            'size:',
            blob.size,
            'type:',
            blob.type
          );

          // Only try to convert if we have a valid blob with size
          if (blob.size > 0) {
            return this.convertBlobToBase64(blob);
          } else {
            console.error('Received empty blob');
            return of('');
          }
        }),
        catchError((error) => {
          console.error('Error getting user image:', error);
          return of('');
        })
      );
  }

  private convertBlobToBase64(blob: Blob): Observable<string> {
    return new Observable((observer) => {
      const reader = new FileReader();

      reader.onloadend = () => {
        const result = reader.result as string;
        console.log('Base64 generado - longitud:', result ? result.length : 0);
        // Only log a preview of the string to avoid console flooding
        if (result) console.log('Preview:', result.substring(0, 50) + '...');

        observer.next(result);
        observer.complete();
      };

      reader.onerror = (error) => {
        console.error('Error al convertir Blob a Base64:', error);
        observer.error(error);
      };

      reader.readAsDataURL(blob);
    });
  }

  getUserById(id: number): Observable<any> {
    return this.http.get(`/api/v1/users/${id}`);
  }

  uploadUserImage(userId: number, formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/${userId}/image`, formData);
  }

  updateUserImage(userId: number, formData: FormData): Observable<any> {
    return this.http.put(`${this.apiUrl}/${userId}/image`, formData);
  }

  updateUserProfile(userData: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/update-profile`, userData, {
      observe: 'response',
    });
  }
}
