import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { PhotoVersionHistory, PhotoVersion, RevertToVersionRequest } from '../models/photo-version.model';

@Component({
  selector: 'app-photo-version-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './photo-version-history.component.html',
  styleUrl: './photo-version-history.component.scss'
})
export class PhotoVersionHistoryComponent implements OnInit, OnChanges {
  @Input() photoId!: string;
  @Input() show: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() versionReverted = new EventEmitter<PhotoVersion>();

  versionHistory?: PhotoVersionHistory;
  loading = false;
  error: string | null = null;

  viewMode: 'timeline' | 'grid' | 'compare' = 'timeline';

  // Preview - FIXED: Renamed to avoid conflict
  showPreview = false;
  currentPreviewVersion?: PhotoVersion;  // Changed from previewVersion
  selectedVersion?: PhotoVersion;

  // Compare mode
  compareFromVersion: number = 0;
  compareToVersion: number = 1;

  constructor(
    private photoService: PhotoService,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    if (this.show && this.photoId) {
      this.loadVersionHistory();
    }
  }

  ngOnChanges(): void {
    if (this.show && this.photoId) {
      this.loadVersionHistory();
    }
  }

  private loadVersionHistory(): void {
    this.loading = true;
    this.error = null;

    this.photoService.getVersionHistory(this.photoId).subscribe({
      next: (history) => {
        this.versionHistory = history;
        this.loading = false;

        // Set default compare versions with better logic
        if (history.versions.length > 1) {
          // Find original version (version 0)
          const originalVersion = history.versions.find(v => v.versionNumber === 0);
          // Find current version
          const currentVersion = history.versions.find(v => v.isCurrent);

          if (originalVersion) {
            this.compareFromVersion = Number(originalVersion.versionNumber);
          } else {
            this.compareFromVersion = Number(history.versions[history.versions.length - 1].versionNumber);
          }

          if (currentVersion) {
            this.compareToVersion = Number(currentVersion.versionNumber);
          } else {
            this.compareToVersion = Number(history.versions[0].versionNumber);
          }
        } else if (history.versions.length === 1) {
          // Only one version available
          this.compareFromVersion = Number(history.versions[0].versionNumber);
          this.compareToVersion = Number(history.versions[0].versionNumber);
        }

        console.log('Version history loaded:', {
          totalVersions: history.versions.length,
          availableVersions: history.versions.map(v => ({
            number: v.versionNumber,
            type: typeof v.versionNumber,
            current: v.isCurrent,
            url: v.imageUrl
          })),
          compareFrom: this.compareFromVersion,
          compareFromType: typeof this.compareFromVersion,
          compareTo: this.compareToVersion,
          compareToType: typeof this.compareToVersion
        });
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load version history';
        this.loading = false;
        console.error('Error loading version history:', error);
      }
    });
  }

  // FIXED: Renamed method to avoid conflict
  showVersionPreview(version: PhotoVersion): void {
    this.currentPreviewVersion = version;
    this.showPreview = true;
  }

  closePreview(): void {
    this.showPreview = false;
    this.currentPreviewVersion = undefined;
  }

  revertToVersion(version: PhotoVersion): void {
    const versionTitle = version.versionNumber === 0 ? 'Original' : `Version ${version.versionNumber}`;

    this.modalService.open(
      'Confirm Revert',
      `Are you sure you want to revert to ${versionTitle}? This will create a new version with the selected image.`,
      ModalType.CONFIRM
    );

    // Subscribe to modal confirmation
    this.modalService.confirm$.subscribe(() => {
      this.performRevert(version);
    });
  }

  private performRevert(version: PhotoVersion): void {
    const request: RevertToVersionRequest = {
      photoId: this.photoId,
      targetVersion: version.versionNumber,
      reason: `Reverted to ${version.versionNumber === 0 ? 'original' : 'version ' + version.versionNumber}`
    };

    console.log('🔄 Starting revert for version:', version);

    this.photoService.revertToVersion(this.photoId, request).subscribe({
      next: (revertedVersion) => {
        this.modalService.open('Success', 'Photo reverted successfully!', ModalType.SUCCESS);
        this.versionReverted.emit(revertedVersion);

        // Reload version history to show the new revert version
        this.loadVersionHistory();
        this.closePreview();
      },
      error: (error) => {
        this.modalService.open('Error', error.error?.message || 'Failed to revert photo', ModalType.ERROR);
        console.error('Error reverting photo:', error);
      }
    });
  }

  getVersionTitle(versionNumber: number): string {
    return versionNumber === 0 ? 'Original' : `Version ${versionNumber}`;
  }

  getVersionImageUrl(versionNumber: number): string {
    if (!this.versionHistory) {
      console.warn('No version history available');
      return '';
    }

    // Convert to number in case it's a string from the dropdown
    const versionNum = typeof versionNumber === 'string' ? parseInt(versionNumber, 10) : versionNumber;

    console.log('Looking for version:', versionNum, 'Type:', typeof versionNum);
    console.log('Available versions:', this.versionHistory.versions.map(v => ({ num: v.versionNumber, type: typeof v.versionNumber, url: v.imageUrl })));

    const version = this.versionHistory.versions.find(v => v.versionNumber === versionNum);
    if (version) {
      console.log('Found version:', versionNum, 'URL:', version.imageUrl);
      return version.imageUrl;
    }

    // Fallback: if version not found, log for debugging and return empty
    console.warn(`Version ${versionNum} not found in versions:`, this.versionHistory.versions.map(v => v.versionNumber));
    return '';
  }

  closeModal(): void {
    this.close.emit();
    this.showPreview = false;
    this.currentPreviewVersion = undefined;
    this.selectedVersion = undefined;
  }

  onCompareSelectionChange(): void {
    // Convert string values to numbers (in case they come from dropdown as strings)
    this.compareFromVersion = typeof this.compareFromVersion === 'string' ?
      parseInt(this.compareFromVersion, 10) : this.compareFromVersion;
    this.compareToVersion = typeof this.compareToVersion === 'string' ?
      parseInt(this.compareToVersion, 10) : this.compareToVersion;

    console.log('Compare selection changed:', {
      from: this.compareFromVersion,
      fromType: typeof this.compareFromVersion,
      to: this.compareToVersion,
      toType: typeof this.compareToVersion,
      fromUrl: this.getVersionImageUrl(this.compareFromVersion),
      toUrl: this.getVersionImageUrl(this.compareToVersion)
    });
  }

  onImageError(side: string, event: any): void {
    console.error(`Image failed to load on ${side} side:`, event);
  }
}
