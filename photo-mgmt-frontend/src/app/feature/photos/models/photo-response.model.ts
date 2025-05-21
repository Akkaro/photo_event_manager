import {PhotoBase} from './photo-base.model';


export interface PhotoResponse extends PhotoBase {
  photoId: string;
  ownerId: string;
  path: string;
  uploadedAt: string;
  isEdited: boolean;
}
