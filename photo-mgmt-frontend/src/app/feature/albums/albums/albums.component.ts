import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AlbumService } from '../../../core/services/album/album.service';
import { AuthService } from '../../../core/services/auth/auth.service';
import { ModalService } from '../../../core/services/modal/modal.service';
import { buildAlbumFilterDTOFromSearchBy } from '../../../core/utils/rest-utils';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { SearchBarComponent } from '../../../shared/components/search-bar/search-bar.component';
import { ModalType } from '../../../shared/models/modal-type.enum';
import { AlbumResponse } from '../models/album-response.model';
import { ROUTES } from '../../../core/config/routes.enum';
import { Subscription } from 'rxjs';
import { Role } from '../../profile/models/user-role.enum';
import { UserResponse } from '../../profile/models/user-response.model';


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
export class AlbumsComponent implements OnInit, OnDestroy {

  albums: AlbumResponse[] = [];
  loading = true;
  error: string | null = null;
  currentPage = 0;
  totalPages = -1;
  loggedUser?: UserResponse;
  userSubscription?: Subscription;

  constructor(
    private albumService: AlbumService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private modalService: ModalService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => {
        this.loggedUser = response;
        this.handleRouteParams();
      });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

  private handleRouteParams(): void {
    this.route.queryParamMap.subscribe(params => {
      const searchBy = params.get('searchBy') ?? '';
      this.currentPage = +(params.get('page') ?? '0');
      this.fetchAlbums(searchBy, this.currentPage);
    });
  }

  private fetchAlbums(searchBy: string, page: number): void {
    if (!this.loggedUser) {
      return; // Don't fetch if user isn't loaded yet
    }

    this.loading = true;
    // Create the filter without ownerId - the backend will handle permissions
    const filter = buildAlbumFilterDTOFromSearchBy(searchBy, page);

    // Use the standard getAll method
    this.albumService.getAll(filter).subscribe({
      next: (response) => {
        this.albums = response.elements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.error = error.error?.message || 'Failed to load albums';
        this.loading = false;
        console.error('Error loading albums:', error);

        // Show a user-friendly error message
        this.modalService.open(
          'Error Loading Albums',
          'There was a problem loading your albums. Please try again later.',
          ModalType.ERROR
        );
      }
    });
  }

  createNewAlbum(): void {
    this.router.navigateByUrl(`/${ROUTES.ALBUMS}/create`);
  }

  onPageChange(page: number): void {
    const currentUrl = this.router.url.split('?')[0];
    this.router.navigate([currentUrl], {
      queryParams: { page },
      queryParamsHandling: 'merge'
    }).then();
  }

  protected readonly Role = Role;
}
