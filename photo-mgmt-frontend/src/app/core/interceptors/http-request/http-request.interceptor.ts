// photo-mgmt-frontend/src/app/core/interceptors/http-request/http-request.interceptor.ts

import { HttpHeaders, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { environment } from '../../../../environments/environment';

export const httpRequestInterceptor: HttpInterceptorFn = (req, next) => {
  // Check if this is a public route that doesn't need authentication
  const isPublicRoute = req.url.includes('/v1/public/') || req.url.includes('/api/v1/public/');

  let modifiedReq;

  if (isPublicRoute) {
    // For public routes, don't add authentication headers or credentials
    modifiedReq = req.clone({
      url: getUrl(req.url),
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }),
      // Don't send credentials for public routes
      withCredentials: false
    });

    console.log('HTTP Interceptor: Public route detected, no auth headers added:', modifiedReq.url);
  } else {
    // For authenticated routes, add the standard headers and credentials
    modifiedReq = req.clone({
      url: getUrl(req.url),
      headers: getHeaders(),
      withCredentials: true
    });

    console.log('HTTP Interceptor: Authenticated route, adding auth headers:', modifiedReq.url);
  }

  return next(modifiedReq);
};

const getUrl = (url: string) => {
  // If the URL already contains the full API URL, don't prepend it again
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }

  return `${environment.apiUrl}${url}`;
};

const getHeaders = (): HttpHeaders => {
  const cookieService = inject(CookieService);
  const jwtToken = cookieService.get('jwt-token');

  return jwtToken !== ''
    ? new HttpHeaders({ 'Authorization': `Bearer ${jwtToken}` })
    : new HttpHeaders();
};
