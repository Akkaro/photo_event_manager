import { Routes } from '@angular/router';
import { ROUTES } from '../../core/config/routes.enum';
import { hasAuthorization } from '../../core/guards/authorization/authorization.guard';


export const routes: Routes = [
  {
    path: ROUTES.EMPTY,
    loadComponent: () => import('./photos/photos.component').then(m => m.PhotosComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR' ]
    }
  },
  {
    path: ROUTES.ID,
    loadComponent: () => import('./photo/photo.component').then(m => m.PhotoComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR' ],
      isSelf: true
    }
  },
  {
    path: ROUTES.ALL,
    redirectTo: ROUTES.EMPTY
  }
];
