import { Routes } from '@angular/router';
import { ROUTES } from '../../core/config/routes.enum';
import { hasAuthorization } from '../../core/guards/authorization/authorization.guard';


export const routes: Routes = [
  {
    path: ROUTES.EMPTY,
    loadComponent: () => import('./albums/albums.component').then(m => m.AlbumsComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR' ]
    }
  },
  {
    path: ROUTES.ID,
    loadComponent: () => import('./album/album.component').then(m => m.AlbumComponent),
    canActivate: [ hasAuthorization ],
    data: {
      requiredRoles: [ 'ADMIN', 'MODERATOR' ],
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
