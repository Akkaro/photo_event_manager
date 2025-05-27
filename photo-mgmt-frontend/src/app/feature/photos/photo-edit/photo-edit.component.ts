// photo-mgmt-frontend/src/app/feature/photos/photo-edit/photo-edit.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { PhotoEditService, PhotoEditRequest } from '../../../core/services/photo-edit/photo-edit.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { PhotoResponse } from '../models/photo-response.model';
import { ROUTES } from '../../../core/config/routes.enum';

@Component({
  selector: 'app-photo-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './photo-edit.component.html',
  styleUrl: './photo-edit.component.scss'
})
export class PhotoEditComponent implements OnInit {
  photoId!: string;
  photo?: PhotoResponse;
  editForm!: FormGroup;
  loading = false;
  processing = false;
  error: string | null = null;
  previewUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private photoService: PhotoService,
    private photoEditService: PhotoEditService,
    private modalService: ModalService
  ) { }

  ngOnInit(): void {
    this.photoId = this.route.snapshot.paramMap.get('id')!;
    if (!this.photoId) {
      this.error = 'Photo ID not found';
      return;
    }

    this.buildForm();
    this.loadPhoto();
  }

  private buildForm(): void {
    this.editForm = this.fb.group({
      brightness: [1.0, [Validators.min(0.1), Validators.max(3.0)]],
      contrast: [1.0, [Validators.min(0.1), Validators.max(3.0)]]
    });
  }

  private loadPhoto(): void {
    this.loading = true;
    this.photoService.getById(this.photoId).subscribe({
      next: (photo) => {
        this.photo = photo;
        this.previewUrl = photo.path;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load photo';
        this.loading = false;
        this.modalService.open('Error', this.error || 'Failed to load photo', ModalType.ERROR);
      }
    });
  }

  onSubmit(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.processing = true;
    const editRequest: PhotoEditRequest = {
      photoId: this.photoId,
      brightness: this.editForm.get('brightness')?.value,
      contrast: this.editForm.get('contrast')?.value
    };

    this.photoEditService.editPhoto(this.photoId, editRequest).subscribe({
      next: (response) => {
        this.processing = false;
        this.modalService.open('Success', 'Photo edited successfully!', ModalType.SUCCESS);

        // Refresh the photo to get the updated version
        this.loadPhoto();
      },
      error: (error) => {
        this.processing = false;
        this.modalService.open('Error', error.error?.message || 'Failed to edit photo', ModalType.ERROR);
        console.error('Photo edit error:', error);
      }
    });
  }

  resetValues(): void {
    this.editForm.patchValue({
      brightness: 1.0,
      contrast: 1.0
    });
  }

  cancel(): void {
    this.router.navigate([`/${ROUTES.PHOTOS}`, this.photoId]);
  }

  get brightnessValue(): number {
    return this.editForm.get('brightness')?.value || 1.0;
  }

  get contrastValue(): number {
    return this.editForm.get('contrast')?.value || 1.0;
  }
}
