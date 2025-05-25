import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ROUTES } from '../../config/routes.enum';
import { AuthService } from '../../services/auth/auth.service';
import { Role } from '../../../feature/profile/models/user-role.enum';


@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit, OnDestroy {

  protected readonly ROUTES = ROUTES;
  protected readonly Role = Role;

  // For users who can access the photo table
  hasPhotoTablePermission: boolean = false;
  userRole?: Role;
  userSubscription?: Subscription;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.userSubscription = this.authService.user$
      .subscribe(response => {
        this.userRole = response?.role;
        this.hasPhotoTablePermission = [Role.ADMIN, Role.MODERATOR].includes(response?.role as Role);
      });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }

  logout(): void {
    this.authService.logout();
  }
}
