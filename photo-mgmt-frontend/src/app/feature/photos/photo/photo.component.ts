import { NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, Subscription, takeUntil } from 'rxjs';
import { ROUTES } from '../../../core/config/routes.enum';
import { AuthService } from '../../../core/services/auth/auth.service';
import { PhotoService } from '../../../core/services/photo/photo.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { UserResponse } from '../../profile/models/user-response.model';
import { Role } from '../../profile/models/user-role.enum';
import { AlbumResponse } from '../../albums/models/album-response.model';
import { AlbumService } from '../../../core/services/album/album.service';
import { PhotoVersionHistoryComponent } from '../../photo-versions/photo-version-history/photo-version-history.component';
import { PhotoVersion } from '../../photo-versions/models/photo-version.model';

@Component({
  selector: 'app-photo',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    PhotoVersionHistoryComponent
  ],
  templateUrl: './photo.component.html',
  styleUrl: './photo.component.scss'
})
export class PhotoComponent implements OnInit, OnDestroy {
  photoId!: string;
  photoForm!: FormGroup;
  editMode = false;
  loading = false;
  subject$ = new Subject<void>();
  userSubscription?: Subscription;
  loggedUser?: UserResponse;
  error: string | null = null;
  private albums: AlbumResponse[] = [];
  private deleteConfirmationPending = false;

  // Version history
  showVersionHistoryModal = false;
  showingOriginal = false;
  currentImageUrl = '';
  originalImageUrl = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private photoService: PhotoService,
    private authService: AuthService,
    private modalService: ModalService,
    private albumService: AlbumService
  ) { }

  ngOnInit(): void {
    this.fetchPhotoId();
    if (this.photoId) {
      this.fetchPhoto();
      this.fetchAlbums();
    } else {
      console.error('PhotoId parameter is missing or invalid');
      this.error = 'Missing or invalid photo ID';
    }
    this.watchPhotoDeletion();
    this.watchUser();
  }

  ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
    this.userSubscription?.unsubscribe();
  }

  private fetchAlbums(): void {
    this.albumService.getAll().subscribe({
      next: (response) => {
        this.albums = response.elements;
      },
      error: (error) => {
        console.error('Error fetching albums:', error);
      }
    });
  }

  getAlbumName(albumId: string): string {
    const album = this.albums.find(a => a.albumId === albumId);
    return album ? album.albumName : 'Unknown Album';
  }

  private fetchPhotoId(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.photoId = id;
      console.log('Fetched photoId:', this.photoId);
    } else {
      console.error('Failed to get photoId from route params');
    }
  }

  private fetchPhoto(): void {
    this.loading = true;
    console.log('Fetching photo with ID:', this.photoId);
    this.photoService.getById(this.photoId).subscribe({
      next: (photo) => {
        console.log('Photo data received:', photo);
        this.photoForm = this.fb.group({
          photoName: [
            { value: photo.photoName, disabled: true },
            [ Validators.required ]
          ],
          albumId: [
            { value: photo.albumId, disabled: true },
            [ Validators.required ]
          ],
          photoId: [
            { value: photo.photoId, disabled: true },
            [ Validators.required ]
          ],
          ownerId: [
            { value: photo.ownerId, disabled: true },
            [ Validators.required ]
          ],
          path: [
            { value: photo.path, disabled: true },
            [ Validators.required ]
          ],
          originalPath: [
            { value: photo.originalPath, disabled: true },
            [ Validators.required ]
          ],
          isEdited: [
            { value: photo.isEdited, disabled: true },
            [ Validators.required ]
          ],
          uploadedAt: [
            { value: new Date(photo.uploadedAt).toISOString().split('T')[0], disabled: true },
            [ Validators.required ]
          ]
        });
        this.photoForm.disable();

        // Set image URLs
        this.currentImageUrl = photo.path;
        this.originalImageUrl = photo.originalPath;

        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error fetching photo:', error);
        this.error = `Failed to load photo: ${error.message}`;
        this.loading = false;
        this.modalService.open('Error', `Failed to load photo: ${error.error?.message || error.message}`, ModalType.ERROR);
      }
    });
  }

  private watchUser(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => this.loggedUser = response);
  }

  toggleEdit(): void {
    if (this.editMode && this.photoForm.invalid) {
      this.photoForm.markAllAsTouched();
      return;
    }

    this.editMode = !this.editMode;

    if (this.editMode) {
      this.photoForm.get('photoName')?.enable();
    } else {
      const updatedPhoto = {
        photoName: this.photoForm.get('photoName')?.value,
        albumId: this.photoForm.get('albumId')?.value
      };

      this.photoService.update(this.photoId, updatedPhoto).subscribe({
        next: () => {
          this.photoForm.disable();
          this.modalService.open('Success', 'Photo has been successfully updated!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error?.message || 'Failed to update photo', ModalType.ERROR);
          this.photoForm.disable();
          this.editMode = false;
        }
      });
    }
  }

  editPhoto(): void {
    this.router.navigate([`/${ROUTES.PHOTOS}`, this.photoId, 'edit']);
  }

  deletePhoto(): void {
    this.deleteConfirmationPending = true;
    this.modalService.open('Delete', 'Are you sure you want to delete this photo?', ModalType.CONFIRM);
  }


  private watchPhotoDeletion(): void {
    this.modalService.confirm$
      .pipe(takeUntil(this.subject$))
      .subscribe((action) => {
        // Only process if we're expecting a delete confirmation
        if (this.deleteConfirmationPending) {
          this.deleteConfirmationPending = false;

          this.photoService.delete(this.photoId).subscribe({
            next: () => {
              this.modalService.open('Deleted', 'Photo has been deleted.', ModalType.SUCCESS);
              this.router.navigateByUrl(ROUTES.PHOTOS).then();
            },
            error: (error: HttpErrorResponse) => {
              this.modalService.open('Error', error.error.message, ModalType.ERROR);
            }
          });
        }
      });
  }

  // NEW: Version history methods
  toggleOriginal(): void {
    this.showingOriginal = !this.showingOriginal;
    this.currentImageUrl = this.showingOriginal ? this.originalImageUrl : this.photoForm.get('path')?.value;
  }

  showVersionHistory(): void {
    this.showVersionHistoryModal = true;
  }

  onVersionReverted(version: PhotoVersion): void {

    console.log('🔄 Version reverted - starting photo refresh');
    console.log('Current photo before refresh:', this.photoForm);
    console.log('Current photoId:', this.photoId);

    // Refresh the photo data after version revert
    setTimeout(() => {
      this.fetchPhoto();
    }, 500);
    this.modalService.open('Success', `Photo reverted to ${version.versionNumber === 0 ? 'original' : 'version ' + version.versionNumber}!`, ModalType.SUCCESS);
  }

  protected readonly Role = Role;
}
