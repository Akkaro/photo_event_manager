import { HttpParams } from '@angular/common/http';
import { PhotoFilter } from '../../feature/photos/models/photo-filter.model';
import { apiConfig } from '../config/api-config';
import {AlbumFilter} from '../../feature/albums/models/album-filter.model';


export const buildPhotoFilterDTOFromSearchBy = (searchBy: string | null, page: number): PhotoFilter => {
  if (!searchBy) {
    return {
      pageNumber: page,
      pageSize: apiConfig.pageSize
    };
  }

  const isNumeric = !isNaN(Number(searchBy));
  const isBoolean = searchBy === 'true' || searchBy === 'false';
  const isDateLike = /^\d{4}-\d{2}-\d{2}(T.*)?(Z|[+-]\d{2}:\d{2})?$/.test(searchBy);

  return {
    albumId: searchBy,
    ownerId: searchBy,
    isEdited: isBoolean ? searchBy === 'true' : undefined,
    uploadedAt: isDateLike ? searchBy : undefined,
    pageNumber: page,
    pageSize: apiConfig.pageSize
  };
};

export const buildPhotoQueryParams = (filter?: PhotoFilter): HttpParams => {
  return Object.entries(filter || {})
    .reduce((params, [ key, value ]) => value != null && value !== '' ? params.set(key, String(value)) : params,
      new HttpParams()
    );
};


export const buildAlbumFilterDTOFromSearchBy = (searchBy: string | null, page: number): AlbumFilter => {
  if (!searchBy) {
    return {
      pageNumber: page,
      pageSize: apiConfig.pageSize
    };
  }

  const isDateLike = /^\d{4}-\d{2}-\d{2}(T.*)?(Z|[+-]\d{2}:\d{2})?$/.test(searchBy);

  return {
    albumName: searchBy,
    ownerId: searchBy,
    createdAt: isDateLike ? searchBy : undefined,
    pageNumber: page,
    pageSize: apiConfig.pageSize
  };
};

export const buildAlbumQueryParams = (filter?: AlbumFilter): HttpParams => {
  return Object.entries(filter || {})
    .reduce((params, [ key, value ]) => value != null && value !== '' ? params.set(key, String(value)) : params,
      new HttpParams()
    );
};
