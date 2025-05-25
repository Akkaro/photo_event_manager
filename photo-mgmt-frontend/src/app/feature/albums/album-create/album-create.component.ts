import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ROUTES } from '../../../core/config/routes.enum';
import { AlbumService } from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-album-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './album-create.component.html',
  styleUrl: './album-create.component.scss'
})
export class AlbumCreateComponent implements OnInit {
  albumForm!: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private albumService: AlbumService,
    private modalService: ModalService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.buildForm();
  }

  private buildForm(): void {
    this.albumForm = this.fb.group({
      albumName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]]
    });
  }

  onSubmit(): void {
    if (this.albumForm.invalid) {
      this.albumForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const albumData = this.albumForm.value;

    this.albumService.save(albumData).subscribe({
      next: (response) => {
        this.loading = false;
        this.modalService.open('Success', 'Album created successfully!', ModalType.SUCCESS);
        this.router.navigateByUrl(`/${ROUTES.ALBUMS}`);
      },
      error: (error) => {
        this.loading = false;
        this.modalService.open('Error', error.error?.message || 'Failed to create album', ModalType.ERROR);
        console.error('Album creation error:', error);
      }
    });
  }

  cancel(): void {
    this.router.navigateByUrl(`/${ROUTES.ALBUMS}`);
  }
}
