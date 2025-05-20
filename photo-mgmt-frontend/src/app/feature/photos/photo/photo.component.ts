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

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private photoService: PhotoService,
    private authService: AuthService,
    private modalService: ModalService
  ) { }

  ngOnInit(): void {
    this.fetchPhotoId();
    this.fetchPhoto();
    this.watchPhotoDeletion();
    this.watchUser();
  }

  ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
    this.userSubscription?.unsubscribe();
  }

  private fetchPhotoId(): void {
    this.photoId = this.route.snapshot.paramMap.get('photoId')!;
  }

  private fetchPhoto(): void {
    this.photoService.getById(this.photoId).subscribe((photo) => {
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
