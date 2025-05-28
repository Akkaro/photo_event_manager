export interface PhotoVersion {
  editId?: string;
  versionNumber: number;
  imageUrl: string;
  editDescription: string;
  createdAt: string;
  ownerName: string;
  isCurrent: boolean;
}

export interface PhotoVersionHistory {
  photoId: string;
  photoName: string;
  originalUrl: string;
  currentUrl: string;
  currentVersion: number;
  totalVersions: number;
  versions: PhotoVersion[];
}

export interface RevertToVersionRequest {
  photoId: string;
  targetVersion: number;
  reason?: string;
}
