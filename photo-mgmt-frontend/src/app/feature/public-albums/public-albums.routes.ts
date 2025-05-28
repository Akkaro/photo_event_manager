import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'album/:token',
    loadComponent: () => import('./public-album-viewer/public-album-viewer.component').then(m => m.PublicAlbumViewerComponent)
  }
];
