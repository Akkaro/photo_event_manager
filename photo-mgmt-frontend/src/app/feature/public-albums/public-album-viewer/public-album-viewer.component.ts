// photo-mgmt-frontend/src/app/feature/public-albums/public-album-viewer/public-album-viewer.component.ts

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PublicAlbumService, PublicPhoto, PublicAlbumResponse } from '../../../core/services/public-album/public-album.service';

@Component({
  selector: 'app-public-album-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container py-4">
      @if (loading) {
      <div class="text-center my-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-3">Loading public album...</p>
      </div>
      } @else if (error) {
      <div class="alert alert-danger text-center">
        <h4><i class="fas fa-exclamation-triangle me-2"></i>Album Not Found</h4>
        <p>{{ error }}</p>
          <p class="mb-0">The album may be private or the link may be invalid.</p>
          <div class="mt-3">
            <small class="text-muted">Debug info: Token = {{ publicToken }}</small>
          </div>
        </div>
      } @else if (album) {
      <!-- Album Header -->
      <div class="row mb-4">
        <div class="col-12">
          <div class="card bg-primary text-white">
            <div class="card-body text-center">
              <h1 class="card-title mb-2">
                <i class="fas fa-images me-2"></i>
      {{ album.albumName }}
      </h1>
      <p class="card-text">
        <i class="fas fa-user me-1"></i> Created by {{ album.ownerName }}
      <span class="mx-2">•</span>
      <i class="fas fa-calendar me-1"></i> {{ album.createdAt | date:'mediumDate' }}
      <span class="mx-2">•</span>
      <i class="fas fa-photo-video me-1"></i> {{ album.photoCount }} photo{{ album.photoCount !== 1 ? 's' : '' }}
      </p>
      <div class="mt-3">
        <span class="badge bg-success fs-6">
          <i class="fas fa-globe me-1"></i>
          Public Album
        </span>
      </div>
    </div>
  </div>
</div>
</div>

<!-- Photos Grid -->
@if (album.photos && album.photos.length > 0) {
      <div class="row g-4">
        @for (photo of album.photos; track photo.photoName) {
        <div class="col-lg-4 col-md-6">
          <div class="card photo-card h-100">
            <div class="photo-container">
              <img
                [src]="photo.path"
                [alt]="photo.photoName"
                class="card-img-top"
                (click)="viewPhotoFullscreen(photo)"
                loading="lazy">

              @if (photo.isEdited) {
        <span class="badge bg-success position-absolute top-0 end-0 m-2">
                        <i class="fas fa-magic"></i> Edited
                      </span>
        }
        </div>

        <div class="card-body">
          <h6 class="card-title">{{ photo.photoName }}</h6>
                    <small class="text-muted">
                      <i class="fas fa-clock me-1"></i>
        {{ photo.uploadedAt | date:'medium' }}
        </small>
      </div>
    </div>
  </div>
        }
      </div>
      } @else {
      <div class="alert alert-info text-center">
        <h5><i class="fas fa-images me-2"></i>No Photos Yet</h5>
        <p class="mb-0">This album doesn't contain any photos yet.</p>
      </div>
      }

      <!-- Album Footer -->
      <div class="text-center mt-5 pt-4 border-top">
        <p class="text-muted">
          <i class="fas fa-info-circle me-1"></i>
          This is a public album shared by {{ album.ownerName }}
      </p>
    </div>
      }

      <!-- Fullscreen Photo Modal -->
      @if (showFullscreen && selectedPhoto) {
      <div class="modal fade show d-block" tabindex="-1" role="dialog">
        <div class="modal-dialog modal-xl modal-dialog-centered">
          <div class="modal-content bg-dark">
            <div class="modal-header border-0">
              <h5 class="modal-title text-white">{{ selectedPhoto.photoName }}</h5>
                <button type="button" class="btn-close btn-close-white" (click)="closeFullscreen()"></button>
              </div>
              <div class="modal-body p-0 d-flex justify-content-center align-items-center">
                <img
                  [src]="selectedPhoto.path"
                  [alt]="selectedPhoto.photoName"
                  class="img-fluid"
                  style="max-height: 80vh; object-fit: contain;">
              </div>
              <div class="modal-footer border-0">
                <small class="text-white-50">
                  <i class="fas fa-clock me-1"></i>
      {{ selectedPhoto.uploadedAt | date:'medium' }}
      @if (selectedPhoto.isEdited) {
      <span class="ms-2">
                      <i class="fas fa-magic me-1"></i>Edited
                    </span>
      }
      </small>
    </div>
  </div>
</div>
</div>
<div class="modal-backdrop fade show"></div>
      }
    </div>
  `,
  styles: [`
    .photo-card {
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      cursor: pointer;
    }

    .photo-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0,0,0,0.15);
    }

    .photo-container {
      position: relative;
      height: 250px;
      overflow: hidden;
    }

    .card-img-top {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }

    .photo-card:hover .card-img-top {
      transform: scale(1.05);
    }

    .modal-xl {
      max-width: 90vw;
    }

    .badge {
      font-size: 0.75rem;
    }

    @media (max-width: 768px) {
      .photo-container {
        height: 200px;
      }

      .modal-xl {
        max-width: 95vw;
        margin: 1rem;
      }
    }
  `]
})
export class PublicAlbumViewerComponent implements OnInit {
  publicToken!: string;
  album: PublicAlbumResponse | null = null;
  loading = true;
  error: string | null = null;

  showFullscreen = false;
  selectedPhoto: PublicPhoto | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private publicAlbumService: PublicAlbumService
  ) {}

  ngOnInit(): void {
    this.publicToken = this.route.snapshot.paramMap.get('token')!;

    console.log('PublicAlbumViewerComponent initialized with token:', this.publicToken);

    if (!this.publicToken) {
      this.error = 'Invalid album link - no token provided';
      this.loading = false;
      return;
    }

    this.loadPublicAlbum();
  }

  private loadPublicAlbum(): void {
    this.loading = true;
    this.error = null;

    console.log('Loading public album with token:', this.publicToken);
    console.log('Making API call to backend...');

    // Use the service to call the backend API
    this.publicAlbumService.getPublicAlbum(this.publicToken)
      .subscribe({
        next: (album) => {
          console.log('Public album loaded successfully:', album);
          this.album = album;
          this.loading = false;

          // Update page title
          document.title = `${album.albumName} - Public Album`;
        },
        error: (error) => {
          console.error('Error loading public album:', error);
          console.error('Error status:', error.status);
          console.error('Error details:', error.error);

          // More detailed error handling
          if (error.status === 404) {
            this.error = 'Album not found. It may be private or the link may be invalid.';
          } else if (error.status === 0) {
            this.error = 'Unable to connect to server. Please check your connection.';
          } else {
            this.error = error.error?.message || `Server error (${error.status}): ${error.statusText}`;
          }

          this.loading = false;
        }
      });
  }

  viewPhotoFullscreen(photo: PublicPhoto): void {
    this.selectedPhoto = photo;
    this.showFullscreen = true;

    // Prevent body scrolling when modal is open
    document.body.style.overflow = 'hidden';
  }

  closeFullscreen(): void {
    this.showFullscreen = false;
    this.selectedPhoto = null;

    // Restore body scrolling
    document.body.style.overflow = '';
  }
}
