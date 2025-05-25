import { NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subject, Subscription, takeUntil } from 'rxjs';
import { ROUTES } from '../../../core/config/routes.enum';
import { AuthService } from '../../../core/services/auth/auth.service';
import { AlbumService } from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { UserResponse } from '../../profile/models/user-response.model';
import { Role } from '../../profile/models/user-role.enum';


@Component({
  selector: 'app-album',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgClass,
    RouterLink
  ],
  templateUrl: './album.component.html',
  styleUrl: './album.component.scss'
})
export class AlbumComponent implements OnInit, OnDestroy {
  albumId!: string;
  albumForm!: FormGroup;
  editMode = false;
  subject$ = new Subject<void>();
  userSubscription?: Subscription;
  loggedUser?: UserResponse;
  isOwner = false;
  loading = false;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private albumService: AlbumService,
    private authService: AuthService,
    private modalService: ModalService
  ) { }

  ngOnInit(): void {
    this.fetchAlbumId();
    this.watchUser();
    this.fetchAlbum();
    this.watchAlbumDeletion();
  }

  ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
    this.userSubscription?.unsubscribe();
  }

  private fetchAlbumId(): void {
    this.albumId = this.route.snapshot.paramMap.get('id')!;
    if (!this.albumId) {
      this.error = 'Album ID not found';
      this.modalService.open('Error', 'Album ID not found', ModalType.ERROR);
    }
  }

  private fetchAlbum(): void {
    this.loading = true;
    this.albumService.getById(this.albumId).subscribe({
      next: (album) => {
        this.albumForm = this.fb.group({
          albumName: [
            { value: album.albumName, disabled: true },
            [ Validators.required, Validators.minLength(2), Validators.maxLength(30) ]
          ],
          albumId: [
            { value: album.albumId, disabled: true },
            [ Validators.required ]
          ],
          ownerId: [
            { value: album.ownerId, disabled: true },
            [ Validators.required ]
          ],
          ownerName: [
            { value: album.ownerName, disabled: true },
            [ Validators.required ]
          ],
          qrCode: [
            { value: album.qrCode, disabled: true },
            [ Validators.required ]
          ],
          createdAt: [
            { value: new Date(album.createdAt).toISOString().split('T')[0], disabled: true },
            [ Validators.required ]
          ]
        });

        // Check if logged user is the owner of this album
        if (this.loggedUser) {
          this.isOwner = this.loggedUser.userId === album.ownerId;
        }

        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.error = error.error?.message || 'Failed to load album';
        this.loading = false;
        // Fix: Use a non-null string for the error message
        this.modalService.open('Error', error.error?.message || 'Failed to load album', ModalType.ERROR);
      }
    });
  }

  private watchUser(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => {
        this.loggedUser = response;
        // If we already have album data, check ownership
        if (this.albumForm && this.albumForm.get('ownerId')?.value) {
          this.isOwner = this.loggedUser?.userId === this.albumForm.get('ownerId')?.value;
        }
      });
  }

  toggleEdit(): void {
    if (this.albumForm.invalid) {
      this.albumForm.markAllAsTouched();
      return;
    }

    this.editMode = !this.editMode;

    if (this.editMode) {
      // Enable only the name field for editing
      this.albumForm.get('albumName')?.enable();
    } else {
      // Save changes
      const updatedAlbum = {
        albumName: this.albumForm.get('albumName')?.value
      };

      this.albumService.update(this.albumId, updatedAlbum).subscribe({
        next: () => {
          this.albumForm.disable();
          this.modalService.open('Success', 'Album has been successfully updated!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error?.message || 'Failed to update album', ModalType.ERROR);
          // Re-disable the form
          this.albumForm.disable();
          this.editMode = false;
        }
      });
    }
  }

  deleteAlbum(): void {
    this.modalService.open('Delete Album', 'Are you sure you want to delete this album? All photos in this album will also be affected.', ModalType.CONFIRM);
  }

  private watchAlbumDeletion(): void {
    this.modalService.confirm$
      .pipe(takeUntil(this.subject$))
      .subscribe(() => {
        this.albumService.delete(this.albumId).subscribe({
          next: () => {
            this.modalService.open('Deleted', 'Album has been deleted.', ModalType.SUCCESS);
            this.router.navigateByUrl(ROUTES.ALBUMS).then();
          },
          error: (error: HttpErrorResponse) => {
            this.modalService.open('Error', error.error?.message || 'Failed to delete album', ModalType.ERROR);
          }
        });
      });
  }

  protected readonly Role = Role;
}
