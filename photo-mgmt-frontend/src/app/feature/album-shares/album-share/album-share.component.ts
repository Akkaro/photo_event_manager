// photo-mgmt-frontend/src/app/feature/albums/album-share/album-share.component.ts
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AlbumService } from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';

export interface AlbumShareResponse {
  albumShareId: string;
  albumId: string;
  albumName: string;
  sharedWithUserId: string;
  sharedWithUserName: string;
  sharedWithUserEmail: string;
  sharedByUserId: string;
  sharedByUserName: string;
  sharedByUserEmail: string;
  sharedAt: string;
}

@Component({
  selector: 'app-album-share',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './album-share.component.html',
  styleUrl: './album-share.component.scss'
})
export class AlbumShareComponent implements OnInit {
  @Input() albumId!: string;
  @Input() isOwner: boolean = false;
  @Input() show: boolean = false;
  @Output() close = new EventEmitter<void>();

  shareForm!: FormGroup;
  shares: AlbumShareResponse[] = [];
  loading = false;
  sharingInProgress = false;

  constructor(
    private fb: FormBuilder,
    private albumService: AlbumService,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    this.buildForm();
    if (this.isOwner && this.show) {
      this.loadShares();
    }
  }

  ngOnChanges(): void {
    if (this.show && this.isOwner) {
      this.loadShares();
    }
  }

  private buildForm(): void {
    this.shareForm = this.fb.group({
      userEmail: ['', [Validators.required, Validators.email]]
    });
  }

  private loadShares(): void {
    if (!this.albumId) return;

    this.loading = true;
    this.albumService.getAlbumShares(this.albumId).subscribe({
      next: (shares) => {
        this.shares = shares;
        this.loading = false;
      },
      error: (error) => {
        this.modalService.open('Error', 'Failed to load album shares', ModalType.ERROR);
        this.loading = false;
      }
    });
  }

  shareAlbum(): void {
    if (this.shareForm.invalid) {
      this.shareForm.markAllAsTouched();
      return;
    }

    const userEmail = this.shareForm.get('userEmail')?.value;
    this.sharingInProgress = true;

    this.albumService.shareAlbum(this.albumId, userEmail).subscribe({
      next: () => {
        this.modalService.open('Success', 'Album shared successfully!', ModalType.SUCCESS);
        this.shareForm.reset();
        this.loadShares();
        this.sharingInProgress = false;
      },
      error: (error) => {
        this.modalService.open('Error', error.error?.message || 'Failed to share album', ModalType.ERROR);
        this.sharingInProgress = false;
      }
    });
  }

  unshareAlbum(userEmail: string): void {
    this.albumService.unshareAlbum(this.albumId, userEmail).subscribe({
      next: () => {
        this.modalService.open('Success', 'Album unshared successfully!', ModalType.SUCCESS);
        this.loadShares();
      },
      error: (error) => {
        this.modalService.open('Error', error.error?.message || 'Failed to unshare album', ModalType.ERROR);
      }
    });
  }

  closeModal(): void {
    this.close.emit();
  }
}
