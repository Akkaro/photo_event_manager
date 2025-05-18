import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {AlbumService} from '../../../core/services/album/album.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { buildAlbumFilterDTOFromSearchBy } from '../../../core/utils/rest-utils';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { AlbumResponse } from '../models/album-response.model';


@Component({
  selector: 'app-albums',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    CommonModule,
    SearchBarComponent,
    PaginationComponent
  ],
  templateUrl: './albums.component.html',
  styleUrl: './albums.component.scss'
})
export class AlbumsComponent implements OnInit {

  albums: AlbumResponse[] = [];
  loading = true;
  error: string | null = null;
  addAlbumForm!: FormGroup;
  showAddForm = false;
  currentPage = 0;
  totalPages = -1;

  constructor(
    private albumService: AlbumService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private modalService: ModalService
  ) {}

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      const searchBy = params.get('searchBy') ?? '';
      this.currentPage = +(params.get('page') ?? '0');
      this.fetchAlbums(searchBy, this.currentPage);
    });
    this.buildAlbumForm();
  }

  private fetchAlbums(searchBy: string, page: number): void {
    this.loading = true;
    const filter = buildAlbumFilterDTOFromSearchBy(searchBy, page);

    this.albumService.getAll(filter).subscribe({
      next: (response) => {
        this.albums = response.elements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load albums';
        this.loading = false;
      }
    });
  }

  private buildAlbumForm(): void {
    this.addAlbumForm = this.fb.group({
      albumName: [ '', [ Validators.required ] ],
      ownerId: [ '', [ Validators.required] ],
    });
  }

  toggleAddForm(): void {
    this.showAddForm = !this.showAddForm;
  }

  saveAlbum(): void {
    if (this.addAlbumForm.valid) {
      const newAlbum = {
        ...this.addAlbumForm.value,
        albumId: this.addAlbumForm.value.albumId,
        albumName: this.addAlbumForm.value.albumName,
        ownerId: this.addAlbumForm.value.ownerId,
        ownerName: this.addAlbumForm.value.ownerName,
        qrCode: this.addAlbumForm.value.qrCode,
        createdAt: new Date(this.addAlbumForm.value.createdAt).toISOString()
      };
      this.albumService.save(newAlbum).subscribe({
        next: (savedAlbum) => {
          this.albums.push(savedAlbum);
          this.addAlbumForm.reset();
          this.showAddForm = false;
          this.modalService.open('Success', 'Album has been successfully added!', ModalType.SUCCESS);
        },
        error: (error: HttpErrorResponse) => {
          this.modalService.open('Error', error.error.message, ModalType.ERROR);
        }
      });
    }
  }

  onPageChange(page: number): void {
    console.log('Page changed to:', page);
  }
}
