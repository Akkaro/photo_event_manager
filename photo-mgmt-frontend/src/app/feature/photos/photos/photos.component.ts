import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { buildPhotoFilterDTOFromSearchBy } from '../../../core/utils/rest-utils';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { PhotoResponse } from '../models/photo-response.model';
import {PhotoFilter} from '../models/photo-filter.model';
import {PhotoVersionHistoryComponent} from '../../photo-versions/photo-version-history/photo-version-history.component';
import {PhotoVersion} from '../../photo-versions/models/photo-version.model';


@Component({
  selector: 'app-photos',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    CommonModule,
    SearchBarComponent,
    PaginationComponent,
    PhotoVersionHistoryComponent  // Add this
  ],
  templateUrl: './photos.component.html',
  styleUrl: './photos.component.scss'
})
export class PhotosComponent implements OnInit {

  photos: PhotoResponse[] = [];
  loading = true;
  error: string | null = null;
  addPhotoForm!: FormGroup;
  showAddForm = false;
  currentPage = 0;
  totalPages = -1;
  currentSearchBy = '';
  selectedFile: File | null = null;
  showVersionHistoryModal = false;
  selectedPhotoId = '';
  constructor(
    private photoService: PhotoService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      // Check for direct filter parameters first
      const albumId = params.get('albumId');
      const searchBy = params.get('searchBy');
      this.currentPage = +(params.get('page') ?? '0');

      // If we have a direct albumId parameter, use it directly
      if (albumId) {
        const filter: PhotoFilter = {
          albumId: albumId,
          pageNumber: this.currentPage,
          pageSize: 10 // You might want to get this from your apiConfig
        };
        this.fetchPhotosWithFilter(filter);
      } else {
        // Otherwise use the searchBy parameter as before
        this.fetchPhotos(searchBy || '', this.currentPage);
      }
    });
    this.buildPhotoForm();
  }

  private fetchPhotosWithFilter(filter: PhotoFilter): void {
    this.loading = true;

    this.photoService.getAll(filter).subscribe({
      next: (response) => {
        this.photos = response.elements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load photos';
        this.loading = false;
      }
    });
  }

  private fetchPhotos(searchBy: string, page: number): void {
    this.loading = true;
    const filter = buildPhotoFilterDTOFromSearchBy(searchBy, page);

    this.photoService.getAll(filter).subscribe({
      next: (response) => {
        this.photos = response.elements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load photos';
        this.loading = false;
      }
    });
  }

  private buildPhotoForm(): void {
    this.addPhotoForm = this.fb.group({
      photoName: [ '', [ Validators.required ] ],
      albumId: [ '', [ Validators.required] ],
    });
  }

  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
  }

  savePhoto(): void {
    if (this.addPhotoForm.valid && this.selectedFile) {  // Add a check for selectedFile
      const newPhoto = {
        ...this.addPhotoForm.value,
        photoId: this.addPhotoForm.value.photoId,
        ownerId: this.addPhotoForm.value.ownerId,
        path: this.addPhotoForm.value.path,
        uploadedAt: new Date(this.addPhotoForm.value.uploadedAt).toISOString(),
        isEdited: this.addPhotoForm.value.isEdited
      };
      this.photoService.save(newPhoto, this.selectedFile).subscribe({  // Pass the file here
        next: (savedPhoto) => {
          this.photos.push(savedPhoto);
          this.addPhotoForm.reset();
          this.showAddForm = false;
          this.modalService.open('Success', 'Photo has been successfully added!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error.message, ModalType.ERROR);
        }
      });
    }
  }

  showPhotoVersions(photoId: string): void {
    this.selectedPhotoId = photoId;
    this.showVersionHistoryModal = true;
  }

  onVersionReverted(version: PhotoVersion): void {
    // Optionally refresh the photos list or show a success message
    this.modalService.open('Success', `Photo reverted to ${version.versionNumber === 0 ? 'original' : 'version ' + version.versionNumber}!`, ModalType.SUCCESS);
  }

  onFileChanged(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  onPageChange(page: number): void {
    console.log('Page changed to:', page);
  }
}
