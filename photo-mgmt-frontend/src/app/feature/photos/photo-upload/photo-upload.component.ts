import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
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

  constructor(
    private fb: FormBuilder,
    private photoService: PhotoService,
    private albumService: AlbumService,
    private modalService: ModalService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.buildForm();
    this.loadAlbums();
  }

  private buildForm(): void {
    this.uploadForm = this.fb.group({
      photoName: ['', [Validators.required, Validators.minLength(3)]],
      albumId: ['', [Validators.required]]
    });
  }

  private loadAlbums(): void {
    this.albumService.getAll().subscribe({
      next: (response) => {
        this.albums = response.elements;
        // Set default selected album if available
        if (this.albums.length > 0) {
          this.uploadForm.get('albumId')?.setValue(this.albums[0].albumId);
        }
      },
      error: (error) => {
        this.modalService.open('Error', 'Failed to load albums', ModalType.ERROR);
        console.error('Error loading albums:', error);
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
        this.router.navigateByUrl(`/${ROUTES.PHOTOS}`);
      },
      error: (error) => {
        this.loading = false;
        this.modalService.open('Error', error.error?.message || 'Failed to upload photo', ModalType.ERROR);
        console.error('Upload error:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigateByUrl(`/${ROUTES.PHOTOS}`);
  }
}
