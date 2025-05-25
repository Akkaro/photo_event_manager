import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AlbumFilter } from '../../../feature/albums/models/album-filter.model';
import { AlbumRequest } from '../../../feature/albums/models/album-request.model';
import { AlbumResponse } from '../../../feature/albums/models/album-response.model';
import { CollectionResponseDTO } from '../../../shared/models/collection-response-dto.model';
import { ROUTES } from '../../config/routes.enum';
import { buildAlbumQueryParams } from '../../utils/rest-utils';


@Injectable({
  providedIn: 'root'
})
export class AlbumService {

  constructor(private http: HttpClient) { }

  getAll(filter?: AlbumFilter): Observable<CollectionResponseDTO<AlbumResponse>> {
    return this.http.get<CollectionResponseDTO<AlbumResponse>>(
      `/v1/${ROUTES.ALBUMS}`,
      { params: buildAlbumQueryParams(filter) }
    );
  }

  getByOwner(ownerId: string, filter?: AlbumFilter): Observable<CollectionResponseDTO<AlbumResponse>> {
    // Make sure we create a proper filter object with defined values
    const filterWithOwner: AlbumFilter = {
      ownerId: ownerId,
      pageNumber: filter?.pageNumber ?? 0,
      pageSize: filter?.pageSize ?? 10,
      albumName: filter?.albumName,
      createdAt: filter?.createdAt
    };

    return this.http.get<CollectionResponseDTO<AlbumResponse>>(
      `/v1/${ROUTES.ALBUMS}`,
      { params: buildAlbumQueryParams(filterWithOwner) }
    );
  }

  getById(albumId: string): Observable<AlbumResponse> {
    return this.http.get<AlbumResponse>(`/v1/${ROUTES.ALBUMS}/${albumId}`);
  }

  save(album: AlbumRequest): Observable<AlbumResponse> {
    return this.http.post<AlbumResponse>(`/v1/${ROUTES.ALBUMS}`, album);
  }

  update(albumId: string, album: AlbumRequest): Observable<AlbumResponse> {
    return this.http.put<AlbumResponse>(`/v1/${ROUTES.ALBUMS}/${albumId}`, album);
  }

  delete(albumId: string): Observable<void> {
    return this.http.delete<void>(`/v1/${ROUTES.ALBUMS}/${albumId}`);
  }
}
