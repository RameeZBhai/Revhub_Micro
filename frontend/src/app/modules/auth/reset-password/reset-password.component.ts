import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container">
      <form [formGroup]="resetPasswordForm" (ngSubmit)="onSubmit()" class="auth-form">
        <h2>Set New Password</h2>
        <p class="subtitle" *ngIf="email">Resetting password for: {{email}}</p>
        
        <div class="form-group">
          <label>OTP Code</label>
          <input type="text" formControlName="otp" placeholder="Enter OTP received in email" class="form-control">
        </div>

        <div class="form-group">
          <label>New Password</label>
          <input type="password" formControlName="newPassword" placeholder="New Password" class="form-control">
        </div>

        <div class="form-group">
          <label>Confirm Password</label>
          <input type="password" formControlName="confirmPassword" placeholder="Confirm New Password" class="form-control">
          <div *ngIf="resetPasswordForm.errors?.['mismatch'] && resetPasswordForm.touched" class="error-text">
            Passwords do not match
          </div>
        </div>

        <button type="submit" [disabled]="resetPasswordForm.invalid || loading" class="btn-primary">
          {{loading ? 'Resetting...' : 'Reset Password'}}
        </button>

        <div *ngIf="message" class="message" [class.error]="isError">{{message}}</div>
        
        <a routerLink="/auth/login" class="link">Back to Login</a>
      </form>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; }
    .auth-form { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
    .form-group { margin-bottom: 1rem; }
    .form-control { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; }
    .btn-primary { width: 100%; padding: 0.75rem; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .btn-primary:disabled { opacity: 0.6; }
    .message { margin-top: 1rem; padding: 0.5rem; border-radius: 4px; background: #d4edda; color: #155724; }
    .message.error { background: #f8d7da; color: #721c24; }
    .error-text { color: #dc3545; font-size: 0.875rem; margin-top: 0.25rem; }
    .link { display: block; text-align: center; margin-top: 1rem; color: #007bff; text-decoration: none; }
    .subtitle { text-align: center; color: #666; margin-bottom: 1.5rem; }
  `]
})
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm: FormGroup;
  loading = false;
  message = '';
  isError = false;
  email = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.resetPasswordForm = this.fb.group({
      otp: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.email = params['email'] || '';
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  onSubmit() {
    if (this.resetPasswordForm.valid && this.email) {
      this.loading = true;
      const { otp, newPassword } = this.resetPasswordForm.value;

      this.authService.resetPassword(this.email, otp, newPassword).subscribe({
        next: (response) => {
          this.message = 'Password reset successfully! Redirecting to login...';
          this.isError = false;
          this.loading = false;
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error) => {
          this.message = error.error || 'Failed to reset password. Invalid OTP?';
          this.isError = true;
          this.loading = false;
        }
      });
    } else if (!this.email) {
      this.message = 'Email is missing. Please try the forgot password flow again.';
      this.isError = true;
    }
  }
}