import { FilterRequest } from '../../../shared/models/filter-request-dto.model';


export interface PhotoFilter extends FilterRequest {
  albumId: string;
  ownerId: string;
  isEdited: boolean;
  uploadedAt: string;
}
