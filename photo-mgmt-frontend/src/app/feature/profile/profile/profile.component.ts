import { Component, OnDestroy, OnInit } from '@angular/core';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth/auth.service';
import { UserResponse } from '../models/user-response.model';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent implements OnInit, OnDestroy {

  user?: UserResponse;
  userSubscription?: Subscription;
  birthDate?: string;

  constructor(private authService: AuthService) { }

  ngOnInit(): void {
    this.userSubscription = this.authService.user$
      .pipe(filter(response => !!response))
      .subscribe(response => {
        this.user = response;
        // Handle potential missing birthDate property
        if (response.ZonedDateTime) {
          this.birthDate = new Date(response.ZonedDateTime).toISOString().split('T')[0];
        } else {
          this.birthDate = new Date().toISOString().split('T')[0]; // Default to today
        }
      });
  }

  ngOnDestroy(): void {
    this.userSubscription?.unsubscribe();
  }
}
