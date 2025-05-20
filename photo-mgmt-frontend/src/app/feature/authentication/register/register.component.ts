import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ROUTES } from '../../../core/config/routes.enum';
import { AuthService } from '../../../core/services/auth/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
  imports: [FormsModule, ReactiveFormsModule]
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  errorMessage?: string;
  successMessage?: string;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      userName: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const registerRequest = { ...this.registerForm.value };
    this.authService.register(registerRequest)
      .subscribe({
        next: () => {
          this.successMessage = 'Registration successful! You can now log in with your credentials.';
          this.errorMessage = undefined;
          this.registerForm.reset();
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.error.message || 'Registration failed. Please try again.';
          this.successMessage = undefined;
        }
      });
  }

  dismissError(): void {
    this.errorMessage = undefined;
  }

  dismissSuccess(): void {
    this.successMessage = undefined;
  }

  goToLogin(): void {
    this.router.navigateByUrl(ROUTES.LOGIN).then();
  }

  private passwordMatchValidator(form: AbstractControl): ValidationErrors | null {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (!password || !confirmPassword) {
      return null;
    }

    return password !== confirmPassword ? { passwordMismatch: true } : null;
  }
}
