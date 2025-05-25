import { Routes } from '@angular/router';
import { ROUTES } from '../../core/config/routes.enum';
import { hasAuthorization } from '../../core/guards/authorization/authorization.guard';


export const routes: Routes = [
  {
    path: ROUTES.EMPTY,
    loadComponent: () => import('./albums/albums.component').then(m => m.AlbumsComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR', 'USER' ]
    }
  },
  {
    path: 'create',
    loadComponent: () => import('./album-create/album-create.component').then(m => m.AlbumCreateComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR', 'USER' ]
    }
  },
  {
    path: ROUTES.ID,
    loadComponent: () => import('./album/album.component').then(m => m.AlbumComponent),
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
