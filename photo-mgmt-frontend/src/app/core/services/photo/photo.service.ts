import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PhotoFilter } from '../../../feature/photos/models/photo-filter.model';
import { PhotoRequest } from '../../../feature/photos/models/photo-request.model';
import { PhotoResponse } from '../../../feature/photos/models/photo-response.model';
import { CollectionResponseDTO } from '../../../shared/models/collection-response-dto.model';
import { ROUTES } from '../../config/routes.enum';
import { buildPhotoQueryParams } from '../../utils/rest-utils';


@Injectable({
  providedIn: 'root'
})
export class PhotoService {

  constructor(private http: HttpClient) { }

  getAll(filter?: PhotoFilter): Observable<CollectionResponseDTO<PhotoResponse>> {
    return this.http.get<CollectionResponseDTO<PhotoResponse>>(
      `/v1/${ROUTES.PHOTOS}`,
      { params: buildPhotoQueryParams(filter) }
    );
  }

  getById(photoId: string): Observable<PhotoResponse> {
    return this.http.get<PhotoResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}`);
  }

  save(photo: PhotoRequest): Observable<PhotoResponse> {
    return this.http.post<PhotoResponse>(`/v1/${ROUTES.PHOTOS}`, photo);
  }

  update(photoId: string, photo: PhotoRequest): Observable<PhotoResponse> {
    return this.http.put<PhotoResponse>(`/v1/${ROUTES.PHOTOS}/${photoId}`, photo);
  }

  delete(photoId: string): Observable<void> {
    return this.http.delete<void>(`/v1/${ROUTES.PHOTOS}/${photoId}`);
  }
}
