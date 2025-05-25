import {AlbumBase} from './album-base.model';


export interface AlbumResponse extends AlbumBase {
  albumId: string;
  ownerId: string;
  ownerName: string;
  qrCode: string;
  createdAt: string;
  isShared?: boolean; // Flag to indicate if this album is shared with current user
  sharedByUser?: string; // Name of user who shared it (if applicable)
}
