import {AlbumBase} from './album-base.model';


export interface AlbumResponse extends AlbumBase {
  albumId: string;
  ownerId: string;
  ownerName: string;
  qrCode: string;
  createdAt: string;
  isShared?: boolean;
  sharedByUser?: string;
}
