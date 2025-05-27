// photo-mgmt-frontend/src/app/feature/photo-edits/photo-edit/photo-edit.component.ts
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { PhotoResponse } from '../../photos/models/photo-response.model';
import { PhotoEditRequest } from '../models/photo-edit-request.model';
import { PhotoEditResponse } from '../models/photo-edit-response.model';
import { EditOperation, EditCategory } from '../models/edit-operation.model';
import { EDIT_OPERATIONS } from '../config/edit-operations.config';
import { ROUTES } from '../../../core/config/routes.enum';
import {PhotoEditService} from '../../../core/services/photo-edit/photo-edit.service';
import {PhotoVersionHistoryComponent} from '../../photo-versions/photo-version-history/photo-version-history.component';
import {PhotoVersion} from '../../photo-versions/models/photo-version.model';

@Component({
  selector: 'app-photo-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    PhotoVersionHistoryComponent  // Add this
  ],
  templateUrl: './photo-edit.component.html',
  styleUrl: './photo-edit.component.scss'
})
export class PhotoEditComponent implements OnInit {
  photoId!: string;
  photo?: PhotoResponse;
  editMode: 'simple' | 'advanced' | 'combined' = 'simple';

  // Forms
  simpleEditForm!: FormGroup;
  advancedEditForm!: FormGroup;
  combinedEditForm!: FormGroup;

  // State
  loading = false;
  processing = false;
  error: string | null = null;
  showOriginal = false;

  // Preview
  originalPreviewUrl: string | null = null;
  currentPreviewUrl: string | null = null;

  // Advanced mode
  selectedOperation?: EditOperation;
  operations = EDIT_OPERATIONS;

  // History
  editHistory: PhotoEditResponse[] = [];
  showVersionHistoryModal = false;

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

    this.initializeForms();
    this.loadPhoto();
  }

  private initializeForms(): void {
    // Simple edit form
    this.simpleEditForm = this.fb.group({
      brightness: [0],
      contrast: [1.0],
      gamma: [1.0]
    });

    // Advanced edit form - dynamic based on selected operation
    this.advancedEditForm = this.fb.group({});

    // Combined edit form
    this.combinedEditForm = this.fb.group({
      brightness: [0],
      contrast: [1.0],
      gamma: [1.0],
      histogramEqualization: [false],
      noiseReduction: [''],
      edgeDetectionType: [''],
      thresholdValue: [0]
    });
  }

  private loadPhoto(): void {
    this.loading = true;
    this.photoService.getById(this.photoId).subscribe({
      next: (photo) => {
        this.photo = photo;
        this.originalPreviewUrl = photo.path;
        this.currentPreviewUrl = photo.path;
        this.loading = false;
      },
      error: (error) => {
        this.error = error.error?.message || 'Failed to load photo';
        this.loading = false;
        this.modalService.open('Error', this.error || 'Failed to load photo', ModalType.ERROR);
      }
    });
  }

  // Edit Mode Management
  onEditModeChange(): void {
    this.selectedOperation = undefined;
    this.resetForms();
  }

  private resetForms(): void {
    this.simpleEditForm.reset({
      brightness: 0,
      contrast: 1.0,
      gamma: 1.0
    });

    this.combinedEditForm.reset({
      brightness: 0,
      contrast: 1.0,
      gamma: 1.0,
      histogramEqualization: false,
      noiseReduction: '',
      edgeDetectionType: '',
      thresholdValue: 0
    });

    this.advancedEditForm = this.fb.group({});
  }

  // Simple Mode Methods
  onSimpleValueChange(): void {
    // Could implement real-time preview here if needed
  }

  resetSimpleValues(): void {
    this.simpleEditForm.reset({
      brightness: 0,
      contrast: 1.0,
      gamma: 1.0
    });
  }

  applySimpleEdit(): void {
    if (this.simpleEditForm.invalid) {
      this.simpleEditForm.markAllAsTouched();
      return;
    }

    this.processing = true;
    const formValues = this.simpleEditForm.value;

    const editRequest: PhotoEditRequest = {
      photoId: this.photoId,
      brightness: formValues.brightness,
      contrast: formValues.contrast,
      gamma: formValues.gamma,
      combinedProcessing: true
    };

    this.photoEditService.editPhoto(this.photoId, editRequest).subscribe({
      next: (response) => {
        this.processing = false;
        this.handleEditSuccess(response);
      },
      error: (error) => {
        this.processing = false;
        this.handleEditError(error);
      }
    });
  }

  // Advanced Mode Methods
  getCategories(): string[] {
    return Object.values(EditCategory);
  }

  getOperationsByCategory(category: string): EditOperation[] {
    return this.operations.filter(op => op.category === category);
  }

  selectOperation(operation: EditOperation): void {
    this.selectedOperation = operation;

    // Build form for operation parameters
    if (operation.requiresParameters && operation.parameters) {
      const formGroup: any = {};
      operation.parameters.forEach(param => {
        formGroup[param.name] = [param.defaultValue];
      });
      this.advancedEditForm = this.fb.group(formGroup);
    } else {
      this.advancedEditForm = this.fb.group({});
    }
  }

  applyAdvancedEdit(): void {
    if (!this.selectedOperation) {
      return;
    }

    this.processing = true;
    const formValues = this.advancedEditForm.value;

    // Call the appropriate service method based on operation
    let editObservable;

    switch (this.selectedOperation.id) {
      case 'brightness-contrast':
        editObservable = this.photoEditService.editBrightnessContrast(
          this.photoId,
          formValues.brightness || 0,
          formValues.contrast || 1.0
        );
        break;

      case 'gamma':
        editObservable = this.photoEditService.editGamma(this.photoId, formValues.gamma);
        break;

      case 'histogram-equalization':
        editObservable = this.photoEditService.editHistogramEqualization(this.photoId);
        break;

      case 'blur':
        editObservable = this.photoEditService.editBlur(
          this.photoId,
          formValues.kernelSize,
          formValues.sigma || 2.0
        );
        break;

      case 'edge-detection':
        editObservable = this.photoEditService.editEdgeDetection(this.photoId, formValues.type);
        break;

      case 'morphological':
        editObservable = this.photoEditService.editMorphological(
          this.photoId,
          formValues.operation,
          formValues.kernelSize,
          formValues.iterations || 1
        );
        break;

      case 'denoise':
        editObservable = this.photoEditService.editDenoise(this.photoId, formValues.type);
        break;

      case 'threshold':
        editObservable = this.photoEditService.editThreshold(
          this.photoId,
          formValues.threshold,
          formValues.type || 'binary'
        );
        break;

      case 'auto-threshold':
        editObservable = this.photoEditService.editAutoThreshold(this.photoId);
        break;

      case 'hsv-convert':
        editObservable = this.photoEditService.editHsvConvert(this.photoId);
        break;

      default:
        this.processing = false;
        this.modalService.open('Error', 'Unknown operation selected', ModalType.ERROR);
        return;
    }

    editObservable.subscribe({
      next: (response) => {
        this.processing = false;
        this.handleEditSuccess(response);
      },
      error: (error) => {
        this.processing = false;
        this.handleEditError(error);
      }
    });
  }

  // Combined Mode Methods
  resetCombinedValues(): void {
    this.combinedEditForm.reset({
      brightness: 0,
      contrast: 1.0,
      gamma: 1.0,
      histogramEqualization: false,
      noiseReduction: '',
      edgeDetectionType: '',
      thresholdValue: 0
    });
  }

  applyCombinedEdit(): void {
    if (this.combinedEditForm.invalid) {
      this.combinedEditForm.markAllAsTouched();
      return;
    }

    this.processing = true;
    const formValues = this.combinedEditForm.value;

    const editRequest: PhotoEditRequest = {
      photoId: this.photoId,
      brightness: formValues.brightness,
      contrast: formValues.contrast,
      gamma: formValues.gamma,
      histogramEqualization: formValues.histogramEqualization,
      noiseReduction: formValues.noiseReduction || undefined,
      edgeDetectionType: formValues.edgeDetectionType || undefined,
      thresholdValue: formValues.thresholdValue > 0 ? formValues.thresholdValue : undefined,
      combinedProcessing: true
    };

    this.photoEditService.editPhoto(this.photoId, editRequest).subscribe({
      next: (response) => {
        this.processing = false;
        this.handleEditSuccess(response);
      },
      error: (error) => {
        this.processing = false;
        this.handleEditError(error);
      }
    });
  }

  // Quick Actions
  applyQuickEdit(action: string): void {
    this.processing = true;
    let editObservable;

    switch (action) {
      case 'histogram-equalization':
        editObservable = this.photoEditService.editHistogramEqualization(this.photoId);
        break;
      case 'denoise-bilateral':
        editObservable = this.photoEditService.editDenoise(this.photoId, 'bilateral');
        break;
      case 'edge-detection-canny':
        editObservable = this.photoEditService.editEdgeDetection(this.photoId, 'canny');
        break;
      case 'auto-threshold':
        editObservable = this.photoEditService.editAutoThreshold(this.photoId);
        break;
      default:
        this.processing = false;
        return;
    }

    editObservable.subscribe({
      next: (response) => {
        this.processing = false;
        this.handleEditSuccess(response);
      },
      error: (error) => {
        this.processing = false;
        this.handleEditError(error);
      }
    });
  }

  // Preview Management
  toggleOriginal(): void {
    this.showOriginal = !this.showOriginal;
    this.currentPreviewUrl = this.showOriginal ? this.originalPreviewUrl : this.photo?.path || null;
  }

  resetPreview(): void {
    this.currentPreviewUrl = this.originalPreviewUrl;
    this.showOriginal = false;
  }

  // Utility Methods
  private handleEditSuccess(response: PhotoEditResponse): void {
    this.modalService.open('Success', 'Photo edited successfully!', ModalType.SUCCESS);

    // Add to history
    this.editHistory.unshift(response);

    // Refresh the photo to get the updated version
    this.loadPhoto();
  }

  private handleEditError(error: any): void {
    this.modalService.open('Error', error.error?.message || 'Failed to edit photo', ModalType.ERROR);
    console.error('Photo edit error:', error);
  }

  getEditDisplayName(edit: PhotoEditResponse): string {
    if (edit.combinedProcessing) {
      return 'Combined Processing';
    }

    if (edit.brightness !== undefined || edit.contrast !== undefined) {
      return 'Brightness & Contrast';
    }
    if (edit.gamma !== undefined) {
      return 'Gamma Correction';
    }
    if (edit.histogramEqualization) {
      return 'Histogram Equalization';
    }
    if (edit.blurKernelSize !== undefined) {
      return 'Gaussian Blur';
    }
    if (edit.edgeDetectionType) {
      return `Edge Detection (${edit.edgeDetectionType})`;
    }
    if (edit.morphologicalOperation) {
      return `Morphological ${edit.morphologicalOperation}`;
    }
    if (edit.noiseReduction) {
      return `Noise Reduction (${edit.noiseReduction})`;
    }
    if (edit.thresholdValue !== undefined) {
      return 'Thresholding';
    }
    if (edit.autoThreshold) {
      return 'Auto Thresholding';
    }
    if (edit.hsvConversion) {
      return 'HSV Conversion';
    }

    return 'Photo Edit';
  }

  showVersionHistory(): void {
    this.showVersionHistoryModal = true;
  }

  onVersionReverted(version: PhotoVersion): void {
    // Refresh the photo data after version revert
    this.loadPhoto();
    this.modalService.open('Success', `Photo reverted to ${version.versionNumber === 0 ? 'original' : 'version ' + version.versionNumber}!`, ModalType.SUCCESS);

    // Update the current preview URL
    this.currentPreviewUrl = version.imageUrl;
  }

  cancel(): void {
    this.router.navigate([`/${ROUTES.PHOTOS}`, this.photoId]);
  }
}
