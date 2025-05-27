import { Routes } from '@angular/router';
import { ROUTES } from '../../core/config/routes.enum';
import { hasAuthorization } from '../../core/guards/authorization/authorization.guard';


export const routes: Routes = [
  {
    path: ROUTES.EMPTY,
    loadComponent: () => import('./photos/photos.component').then(m => m.PhotosComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR', 'USER' ]
    }
  },
  {
    path: ROUTES.UPLOAD,
    loadComponent: () => import('./photo-upload/photo-upload.component').then(m => m.PhotoUploadComponent),
  },
  {
    path: `${ROUTES.ID}/edit`,
    loadComponent: () => import('./photo-edit/photo-edit.component').then(m => m.PhotoEditComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR', 'USER' ],
      isSelf: true
    },
    providers: [
      { provide: 'renderMode', useValue: 'clientOnly' }
    ]
  },
  {
    path: ROUTES.ID,
    loadComponent: () => import('./photo/photo.component').then(m => m.PhotoComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR', 'USER' ],
      isSelf: true
    },
    providers: [
      { provide: 'renderMode', useValue: 'clientOnly' }
    ]
  },
  {
    path: ROUTES.ALL,
    redirectTo: ROUTES.EMPTY
  }
];
