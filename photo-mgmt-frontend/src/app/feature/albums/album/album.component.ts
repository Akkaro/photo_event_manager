import { NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
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
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NgClass
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
    this.fetchAlbum();
    this.watchAlbumDeletion();
    this.watchUser();
  }

  ngOnDestroy(): void {
    this.subject$.next();
    this.subject$.complete();
    this.userSubscription?.unsubscribe();
  }

  private fetchAlbumId(): void {
    this.albumId = this.route.snapshot.paramMap.get('albumId')!;
  }

  private fetchAlbum(): void {
    this.albumService.getById(this.albumId).subscribe((album) => {
      this.albumForm = this.fb.group({
        albumName: [
          { value: album.albumName, disabled: true },
          [ Validators.required ]
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
    });
  }

  private watchUser(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => this.loggedUser = response);
  }

  toggleEdit(): void {
    if (this.albumForm.invalid) {
      this.albumForm.markAllAsTouched();
      return;
    }

    this.editMode = !this.editMode;

    if (this.editMode) {
      this.albumForm.enable();
      this.albumForm.get('albumId')?.disable();
    } else {
      const updatedAlbum = {
        ...this.albumForm.getRawValue(),
        createdAt: new Date(this.albumForm.value.createdAt).toISOString()
      };

      this.albumService.update(updatedAlbum.id, updatedAlbum).subscribe({
        next: () => {
          this.albumForm.disable();
          this.modalService.open('Success', 'Album has been successfully updated!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error.message, ModalType.ERROR);
        }
      });
    }
  }

  deleteAlbum(): void {
    this.modalService.open('Delete', 'Are you sure you want to delete this album?', ModalType.CONFIRM);
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
            this.modalService.open('Error', error.error.message, ModalType.ERROR);
          }
        });
      });
  }

  protected readonly Role = Role;
}
