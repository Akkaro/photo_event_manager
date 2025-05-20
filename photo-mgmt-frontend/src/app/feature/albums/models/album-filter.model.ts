import { FilterRequest } from '../../../shared/models/filter-request-dto.model';


export interface AlbumFilter extends FilterRequest {
  albumName?: string;
  ownerId?: string;
  createdAt?: string;
}
