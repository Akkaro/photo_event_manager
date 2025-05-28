// photo-mgmt-frontend/src/app/feature/photos/photo-upload/photo-upload.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { AlbumService } from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { AlbumResponse } from '../../albums/models/album-response.model';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { CommonModule } from '@angular/common';
import { ROUTES } from '../../../core/config/routes.enum';

@Component({
  selector: 'app-photo-upload',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './photo-upload.component.html',
  styleUrl: './photo-upload.component.scss'
})
export class PhotoUploadComponent implements OnInit {
  uploadForm!: FormGroup;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  albums: AlbumResponse[] = [];
  loading = false;
  preselectedAlbumId: string | null = null;
  preselectedAlbumName: string | null = null;
  isAlbumLocked = false; // NEW: Flag to determine if album selection is locked

  constructor(
    private fb: FormBuilder,
    private photoService: PhotoService,
    private albumService: AlbumService,
    private modalService: ModalService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // ENHANCED: More detailed logging and route param handling
    this.route.queryParamMap.subscribe(params => {
      console.log('All query params:', params.keys.map(key => `${key}=${params.get(key)}`));

      this.preselectedAlbumId = params.get('albumId');
      console.log('Raw preselectedAlbumId from params:', this.preselectedAlbumId);

      // If we have a preselected album ID, lock the album selection
      this.isAlbumLocked = !!this.preselectedAlbumId && this.preselectedAlbumId.trim() !== '';

      console.log('Upload component - preselectedAlbumId:', this.preselectedAlbumId);
      console.log('Upload component - isAlbumLocked:', this.isAlbumLocked);
      console.log('Upload component - trimmed and truthy check:', !!this.preselectedAlbumId && this.preselectedAlbumId.trim() !== '');
    });

    this.buildForm();
    this.loadAlbums();
  }

  private buildForm(): void {
    this.uploadForm = this.fb.group({
      photoName: ['', [Validators.required, Validators.minLength(3)]],
      albumId: [this.preselectedAlbumId || '', [Validators.required]]
    });
  }

  private loadAlbums(): void {
    console.log('Loading albums, preselectedAlbumId at load time:', this.preselectedAlbumId);

    this.albumService.getAll().subscribe({
      next: (response) => {
        this.albums = response.elements;
        console.log('Loaded albums:', this.albums.map(a => ({ id: a.albumId, name: a.albumName })));

        if (this.preselectedAlbumId && this.preselectedAlbumId.trim() !== '') {
          console.log('Processing preselected album:', this.preselectedAlbumId);

          // Find the preselected album to get its name
          const preselectedAlbum = this.albums.find(album => album.albumId === this.preselectedAlbumId);
          console.log('Found preselected album:', preselectedAlbum);

          if (preselectedAlbum) {
            this.preselectedAlbumName = preselectedAlbum.albumName;
            this.uploadForm.get('albumId')?.setValue(this.preselectedAlbumId);
            this.isAlbumLocked = true;
            console.log('Album locked successfully:', {
              id: this.preselectedAlbumId,
              name: this.preselectedAlbumName,
              locked: this.isAlbumLocked
            });
          } else {
            console.warn('Preselected album not found in loaded albums');
            console.log('Available album IDs:', this.albums.map(a => a.albumId));
            console.log('Looking for:', this.preselectedAlbumId);

            // If preselected album not found, unlock the selection
            this.isAlbumLocked = false;
            this.preselectedAlbumId = null;
            this.preselectedAlbumName = null;
          }
        } else {
          console.log('No preselected album or empty string, showing dropdown');
          // No preselected album, set the first one as default if available
          if (this.albums.length > 0) {
            this.uploadForm.get('albumId')?.setValue(this.albums[0].albumId);
          }
        }

        console.log('Final state after loadAlbums:', {
          isAlbumLocked: this.isAlbumLocked,
          preselectedAlbumName: this.preselectedAlbumName,
          preselectedAlbumId: this.preselectedAlbumId,
          formValue: this.uploadForm.get('albumId')?.value
        });
      },
      error: (error) => {
        console.error('Error loading albums:', error);
        this.modalService.open('Error', 'Failed to load albums', ModalType.ERROR);
      }
    });
  }

  onFileChanged(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];

      // Create image preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onSubmit(): void {
    if (this.uploadForm.invalid || !this.selectedFile) {
      this.uploadForm.markAllAsTouched();
      if (!this.selectedFile) {
        this.modalService.open('Error', 'Please select an image to upload', ModalType.ERROR);
      }
      return;
    }

    this.loading = true;
    const photoData = this.uploadForm.value;

    this.photoService.save(photoData, this.selectedFile).subscribe({
      next: (response) => {
        this.loading = false;
        this.modalService.open('Success', 'Photo uploaded successfully!', ModalType.SUCCESS);

        // If we came from an album, go back to that album's photos
        if (this.preselectedAlbumId) {
          this.router.navigate([`/${ROUTES.PHOTOS}`], {
            queryParams: { albumId: this.preselectedAlbumId }
          });
        } else {
          this.router.navigateByUrl(`/${ROUTES.PHOTOS}`);
        }
      },
      error: (error) => {
        this.loading = false;
        this.modalService.open('Error', error.error?.message || 'Failed to upload photo', ModalType.ERROR);
        console.error('Upload error:', error);
      }
    });
  }

  cancel(): void {
    // If we came from an album, go back to that album
    if (this.preselectedAlbumId) {
      this.router.navigate([`/${ROUTES.PHOTOS}`], {
        queryParams: { albumId: this.preselectedAlbumId }
      });
    } else {
      this.router.navigateByUrl(`/${ROUTES.PHOTOS}`);
    }
  }
}
