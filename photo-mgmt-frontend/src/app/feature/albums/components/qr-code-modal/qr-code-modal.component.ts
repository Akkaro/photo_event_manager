import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlbumService } from '../../../../core/services/album/album.service';
import { ModalService } from '../../../../core/services/modal/modal.service';
import { ModalType } from '../../../../shared/models/modal-type.enum';
import { PublicAlbumUrlResponse } from '../../models/public-album-url-response.model';

@Component({
  selector: 'app-qr-code-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (show) {
      <div class="modal fade show d-block" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-lg">
          <div class="modal-content">
            <div class="modal-header bg-success text-white">
              <h5 class="modal-title">
                <i class="fas fa-qrcode me-2"></i>
                {{ isPublic ? 'Public Album QR Code' : 'Make Album Public' }}
              </h5>
              <button type="button" class="btn-close btn-close-white" (click)="closeModal()"></button>
            </div>

            <div class="modal-body text-center">
              @if (loading) {
                <div class="d-flex justify-content-center align-items-center" style="height: 200px;">
                  <div class="spinner-border text-success" role="status">
                    <span class="visually-hidden">Loading...</span>
                  </div>
                  <span class="ms-3">{{ loadingMessage }}</span>
                </div>
              } @else if (error) {
                <div class="alert alert-danger">
                  <i class="fas fa-exclamation-triangle me-2"></i>
                  {{ error }}
                </div>
              } @else if (publicAlbumData && publicAlbumData.qrCodeBase64) {
                <!-- QR Code Display -->
                <div class="mb-4">
                  <img
                    [src]="'data:image/png;base64,' + publicAlbumData.qrCodeBase64"
                    alt="QR Code for Album"
                    class="img-fluid border rounded shadow"
                    style="max-width: 300px;">
                </div>

                <!-- Public URL -->
                <div class="mb-4">
                  <label class="form-label fw-bold">Public Album URL:</label>
                  <div class="input-group">
                    <input
                      type="text"
                      class="form-control"
                      [value]="publicAlbumData.publicUrl"
                      readonly
                      #urlInput>
                    <button
                      class="btn btn-outline-secondary"
                      type="button"
                      (click)="copyToClipboard(urlInput.value)">
                      <i class="fas fa-copy"></i> Copy
                    </button>
                  </div>
                  <small class="text-muted">Share this URL or QR code to give others access to your album</small>
                </div>

                <!-- Action Buttons -->
                <div class="d-flex gap-2 justify-content-center">
                  <button
                    class="btn btn-primary"
                    (click)="downloadQRCode()"
                    [disabled]="downloadingQR">
                    @if (downloadingQR) {
                      <span class="spinner-border spinner-border-sm me-1"></span>
                      Downloading...
                    } @else {
                      <i class="fas fa-download me-1"></i>
                      Download QR Code
                    }
                  </button>

                  <button
                    class="btn btn-warning"
                    (click)="makeAlbumPrivate()"
                    [disabled]="makingPrivate">
                    @if (makingPrivate) {
                      <span class="spinner-border spinner-border-sm me-1"></span>
                      Making Private...
                    } @else {
                      <i class="fas fa-lock me-1"></i>
                      Make Private
                    }
                  </button>
                </div>
              } @else if (!isPublic) {
                <!-- Make Public Interface -->
                <div class="text-center py-4">
                  <i class="fas fa-globe fa-4x text-success mb-3"></i>
                  <h5>Make Album Public</h5>
                  <p class="text-muted mb-4">
                    Making your album public will generate a QR code and shareable link that anyone can use to view your photos.
                  </p>

                  <button
                    class="btn btn-success btn-lg"
                    (click)="makeAlbumPublic()"
                    [disabled]="makingPublic">
                    @if (makingPublic) {
                      <span class="spinner-border spinner-border-sm me-2"></span>
                      Generating QR Code...
                    } @else {
                      <i class="fas fa-qrcode me-2"></i>
                      Generate QR Code & Public Link
                    }
                  </button>
                </div>
              }
            </div>

            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">
                Close
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="modal-backdrop fade show"></div>
    }
  `,
  styles: [`
    .modal-dialog {
      margin-top: 3vh;
    }

    .qr-code-container {
      background: #f8f9fa;
      border-radius: 0.5rem;
      padding: 1rem;
    }

    .btn:disabled {
      opacity: 0.6;
    }

    .border {
      border: 2px solid #dee2e6 !important;
    }

    .shadow {
      box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075) !important;
    }
  `]
})
export class QrCodeModalComponent implements OnInit {
  @Input() albumId!: string;
  @Input() albumName!: string;
  @Input() isPublic: boolean = false;
  @Input() show: boolean = false;
  @Output() close = new EventEmitter<void>();
  @Output() albumUpdated = new EventEmitter<void>();

  loading = false;
  loadingMessage = '';
  error: string | null = null;
  publicAlbumData: PublicAlbumUrlResponse | null = null;

  makingPublic = false;
  makingPrivate = false;
  downloadingQR = false;

  constructor(
    private albumService: AlbumService,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    if (this.show && this.isPublic) {
      // If album is already public, we need to get the current QR code
      // Since the backend doesn't have a separate endpoint to get existing QR code,
      // we'll handle this in the parent component
    }
  }

  makeAlbumPublic(): void {
    this.makingPublic = true;
    this.loading = true;
    this.loadingMessage = 'Generating QR code and public link...';
    this.error = null;

    this.albumService.makeAlbumPublic(this.albumId).subscribe({
      next: (response) => {
        this.publicAlbumData = response;
        this.isPublic = true;
        this.makingPublic = false;
        this.loading = false;
        this.albumUpdated.emit();
        this.modalService.open('Success', 'Album is now public! QR code generated successfully.', ModalType.SUCCESS);
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to make album public';
        this.makingPublic = false;
        this.loading = false;
        console.error('Error making album public:', error);
      }
    });
  }

  makeAlbumPrivate(): void {
    this.makingPrivate = true;

    this.albumService.makeAlbumPrivate(this.albumId).subscribe({
      next: () => {
        this.makingPrivate = false;
        this.isPublic = false;
        this.publicAlbumData = null;
        this.albumUpdated.emit();
        this.modalService.open('Success', 'Album is now private. QR code and public link have been removed.', ModalType.SUCCESS);
        this.closeModal();
      },
      error: (error) => {
        this.makingPrivate = false;
        this.modalService.open('Error', error.error?.message || 'Failed to make album private', ModalType.ERROR);
        console.error('Error making album private:', error);
      }
    });
  }

  downloadQRCode(): void {
    this.downloadingQR = true;

    this.albumService.downloadQRCode(this.albumId).subscribe({
      next: (blob) => {
        // Create download link
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.albumName}-qr-code.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        this.downloadingQR = false;
        this.modalService.open('Success', 'QR code downloaded successfully!', ModalType.SUCCESS);
      },
      error: (error) => {
        this.downloadingQR = false;
        this.modalService.open('Error', error.error?.message || 'Failed to download QR code', ModalType.ERROR);
        console.error('Error downloading QR code:', error);
      }
    });
  }

  copyToClipboard(text: string): void {
    navigator.clipboard.writeText(text).then(() => {
      this.modalService.open('Success', 'URL copied to clipboard!', ModalType.SUCCESS);
    }).catch(() => {
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      this.modalService.open('Success', 'URL copied to clipboard!', ModalType.SUCCESS);
    });
  }

  closeModal(): void {
    this.close.emit();
    this.error = null;
    this.loading = false;
  }
}
