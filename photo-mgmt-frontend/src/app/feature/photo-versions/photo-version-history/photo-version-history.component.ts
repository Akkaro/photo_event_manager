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

        // Set default compare versions
        if (history.versions.length > 1) {
          this.compareFromVersion = 0; // Original
          this.compareToVersion = history.currentVersion; // Current
        }
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
    if (!this.versionHistory) return '';

    const version = this.versionHistory.versions.find(v => v.versionNumber === versionNumber);
    return version?.imageUrl || '';
  }

  closeModal(): void {
    this.close.emit();
    this.showPreview = false;
    this.currentPreviewVersion = undefined;
    this.selectedVersion = undefined;
  }
}
