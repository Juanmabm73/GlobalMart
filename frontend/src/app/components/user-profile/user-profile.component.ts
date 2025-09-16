import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { AuthService } from '../../service/auth/auth.service';
import { UserProfileService } from '../../service/user-profile.service';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css'],
  standalone: false,
})
export class UserProfileComponent implements OnInit {
  profileForm!: FormGroup;
  user: any = null;
  loading = false;
  errorMessage = '';
  successMessage = '';
  userImageUrl: string | null = null;
  showPhotoModal = false;
  selectedFile: File | null = null;
  imagePreview: string | null = null;
  uploadingPhoto = false;
  isUser = false;
  isCompany = false;

  constructor(
    private authService: AuthService,
    private userService: UserProfileService,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadUserProfile();
  }

  initializeForm(): void {
    this.profileForm = new FormGroup({
      name: new FormControl('', []),
      username: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.minLength(6)]),
      confirmPassword: new FormControl(''),
    });
  }

  loadUserProfile(): void {
    this.user = null; // Reset user to show loading
    this.authService.getUserProfile().subscribe({
      next: (userData) => {
        this.user = userData;
        this.populateForm(userData);
        this.loadUserImage();

        // Determinar el rol del usuario
        if (this.user && this.user.roles) {
          this.isUser = this.user.roles.includes('USER');
          this.isCompany = this.user.roles.includes('COMPANY');
        }
      },
      error: (err) => {
        console.error('Error loading user profile', err);
        this.errorMessage = 'Failed to load user profile. Please try again.';

        // Redirect to login if unauthorized
        if (err.status === 401) {
          this.router.navigate(['/login']);
        }
      },
    });
  }

  populateForm(user: any): void {
    this.profileForm.patchValue({
      name: user.name || '',
      username: user.username || '',
      email: user.email || '',
      password: '',
      confirmPassword: '',
    });
  }

  loadUserImage(): void {
    if (!this.user || !this.user.id) {
      return;
    }

    this.userService.getUserImage(this.user.id).subscribe({
      next: (imageUrl: string) => {
        this.userImageUrl = imageUrl;
      },
      error: (err: any) => {
        console.log('No profile image found or error loading image', err);
        this.userImageUrl = null;
      },
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const formValues = this.profileForm.value;

    // Only include password if it's not empty
    const userData = {
      name: formValues.name,
      username: formValues.username,
      email: formValues.email,
      password: formValues.password || null,
    };

    this.userService.updateUserProfile(userData).subscribe({
      next: (response: { status: number }) => {
        this.loading = false;
        this.successMessage = 'Profile updated successfully!';

        // If username changed, need to update auth service and reload
        if (response.status === 205) {
          setTimeout(() => {
            this.authService.logout().subscribe(() => {
              this.router.navigate(['/login'], {
                queryParams: { updated: true },
              });
            });
          }, 1500);
        } else {
          // Just refresh profile data
          this.authService.getUserProfile().subscribe((updatedUser) => {
            this.user = updatedUser;
            this.populateForm(updatedUser);
          });
        }
      },
      error: (err: { error: string }) => {
        this.loading = false;
        this.errorMessage =
          err.error || 'Failed to update profile. Please try again.';
        console.error('Error updating profile', err);
      },
    });
  }

  passwordsNotMatching(): boolean {
    const password = this.profileForm.get('password')?.value;
    const confirmPassword = this.profileForm.get('confirmPassword')?.value;
    return (
      password !== confirmPassword &&
      this.profileForm.get('confirmPassword')?.touched === true
    );
  }

  resetForm(): void {
    this.populateForm(this.user);
    this.errorMessage = '';
    this.successMessage = '';
  }

  openPhotoModal(): void {
    this.showPhotoModal = true;
    this.selectedFile = null;
    this.imagePreview = null;
  }

  closePhotoModal(): void {
    this.showPhotoModal = false;
    this.selectedFile = null;
    this.imagePreview = null;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length) {
      this.selectedFile = input.files[0];

      // Create preview
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result as string;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  uploadProfileImage(): void {
    if (!this.selectedFile || !this.user?.id) {
      return;
    }

    this.uploadingPhoto = true;
    const formData = new FormData();
    formData.append('imageFile', this.selectedFile); // Changed from 'file' to 'imageFile'

    this.userService.uploadUserImage(this.user.id, formData).subscribe({
      next: () => {
        this.uploadingPhoto = false;
        this.loadUserImage(); // Reload image
        this.closePhotoModal();
        this.successMessage = 'Profile picture updated successfully!';
      },
      error: (err: any) => {
        this.uploadingPhoto = false;
        console.error('Error uploading profile image', err);
        this.errorMessage =
          'Failed to upload profile picture. Please try again.';
        this.closePhotoModal();
      },
    });
  }

  handleImageError(event: Event): void {
    const img = event.target as HTMLImageElement;
    img.src = '/images/perfil.png'; // Ruta a la imagen en public/images
  }

  // Utility function to safely sanitize image URLs
  getSafeUrl(url: string): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl(url);
  }
}
