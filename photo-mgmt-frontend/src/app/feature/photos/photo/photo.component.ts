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
import {AlbumResponse} from '../../albums/models/album-response.model';
import {AlbumService} from '../../../core/services/album/album.service';


@Component({
  selector: 'app-photo',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgClass
  ],
  templateUrl: './photo.component.html',
  styleUrl: './photo.component.scss'
})
export class PhotoComponent implements OnInit, OnDestroy {
  photoId!: string;
  photoForm!: FormGroup;
  editMode = false;
  subject$ = new Subject<void>();
  userSubscription?: Subscription;
  loggedUser?: UserResponse;
  error: string | null = null;
  private albums: AlbumResponse[] = [];

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
      this.fetchAlbums(); // Add this
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
    // Get the photoId from the route params using the id parameter
    // This parameter name should match what's defined in your routes
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.photoId = id;
      console.log('Fetched photoId:', this.photoId);
    } else {
      console.error('Failed to get photoId from route params');
    }
  }

  private fetchPhoto(): void {
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
          isEdited: [
            { value: photo.isEdited, disabled: true },
            [ Validators.required ]
          ],
          uploadedAt: [
            { value: new Date(photo.uploadedAt).toISOString().split('T')[0], disabled: true },
            [ Validators.required ]
          ]
        });
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error fetching photo:', error);
        this.error = `Failed to load photo: ${error.message}`;
        this.modalService.open('Error', `Failed to load photo: ${error.error?.message || error.message}`, ModalType.ERROR);
      }
    });
  }

  private watchUser(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => this.loggedUser = response);
  }

  toggleEdit(): void {
    if (this.photoForm.invalid) {
      this.photoForm.markAllAsTouched();
      return;
    }

    this.editMode = !this.editMode;

    if (this.editMode) {
      this.photoForm.enable();
      this.photoForm.get('photoId')?.disable();
    } else {
      const updatedPhoto = {
        ...this.photoForm.getRawValue(),
        uploadedAt: new Date(this.photoForm.value.uploadedAt).toISOString()
      };

      this.photoService.update(updatedPhoto.photoId, updatedPhoto).subscribe({
        next: () => {
          this.photoForm.disable();
          this.modalService.open('Success', 'Photo has been successfully updated!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error.message, ModalType.ERROR);
        }
      });
    }
  }

  deletePhoto(): void {
    this.modalService.open('Delete', 'Are you sure you want to delete this photo?', ModalType.CONFIRM);
  }

  private watchPhotoDeletion(): void {
    this.modalService.confirm$
      .pipe(takeUntil(this.subject$))
      .subscribe(() => {
        this.photoService.delete(this.photoId).subscribe({
          next: () => {
            this.modalService.open('Deleted', 'Photo has been deleted.', ModalType.SUCCESS);
            this.router.navigateByUrl(ROUTES.PHOTOS).then();
          },
          error: (error: HttpErrorResponse) => {
            this.modalService.open('Error', error.error.message, ModalType.ERROR);
          }
        });
      });
  }

  protected readonly Role = Role;
}
