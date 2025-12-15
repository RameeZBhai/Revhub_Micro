import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { Post } from './post.service';

export interface User {
  id: number;
  username: string;
  email: string;
  profilePicture?: string;
  bio?: string;
  isPrivate?: boolean;
  createdDate: string;
  followersCount?: number;
  followingCount?: number;
  followStatus?: string;
}

export interface FollowRequest {
  id: number;
  follower: User;
  following: User;
  status: 'PENDING' | 'ACCEPTED';
  createdDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = '/api/profile';

  constructor(private http: HttpClient) { }

  getProfile(username: string): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${username}`);
  }

  getUserPosts(username: string): Observable<Post[]> {
    return this.http.get<any>(`${this.apiUrl}/${username}/posts`).pipe(
      map(response => Array.isArray(response) ? response : []),
      catchError(() => of([]))
    );
  }

  updateProfile(updates: { bio?: string; profilePicture?: string; isPrivate?: string }): Observable<User> {
    return this.http.put<User>(this.apiUrl, updates);
  }

  updateProfileWithFile(formData: FormData): Observable<User> {
    const profileData: any = {};

    // If no file, just send other data
    if (!formData.has('profilePicture') || !(formData.get('profilePicture') instanceof File)) {
      formData.forEach((value, key) => {
        profileData[key] = value;
      });
      return this.http.put<User>(this.apiUrl, profileData);
    }

    // Handle file upload with base64 conversion
    return new Observable<User>(observer => {
      const file = formData.get('profilePicture') as File;
      const reader = new FileReader();

      formData.forEach((value, key) => {
        if (key !== 'profilePicture') {
          profileData[key] = value;
        }
      });

      reader.onload = () => {
        profileData.profilePicture = reader.result as string;
        this.http.put<User>(this.apiUrl, profileData).subscribe({
          next: result => {
            observer.next(result);
            observer.complete();
          },
          error: err => observer.error(err)
        });
      };
      reader.readAsDataURL(file);
    });
  }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/all`);
  }

  followUser(username: string): Observable<{ message: string }> {
    const currentUser = this.getCurrentUser();
    return this.http.post<{ message: string }>(`/api/follow/${currentUser}/follow/${username}`, {}).pipe(
      catchError(() => of({ message: 'Follow request sent' }))
    );
  }

  unfollowUser(username: string): Observable<{ message: string }> {
    const currentUser = this.getCurrentUser();
    return this.http.delete<{ message: string }>(`/api/follow/${currentUser}/unfollow/${username}`);
  }

  getFollowStatus(username: string): Observable<{ status: string }> {
    const currentUser = this.getCurrentUser();
    return this.http.get<{ status: string }>(`/api/follow/${currentUser}/status/${username}`);
  }

  private getCurrentUser(): string {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    console.log('Current user from localStorage:', user);
    return user.username || 'suhas'; // Use your actual username as fallback
  }

  getPendingFollowRequests(): Observable<FollowRequest[]> {
    return this.http.get<FollowRequest[]>(`${this.apiUrl}/follow-requests`);
  }

  acceptFollowRequest(followId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/follow-requests/${followId}/accept`, {});
  }

  rejectFollowRequest(followId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/follow-requests/${followId}/reject`, {});
  }

  cancelFollowRequest(username: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`/api/follow/cancel/${username}`);
  }

  getFollowers(username: string): Observable<User[]> {
    return this.http.get<string[]>(`/api/follow/${username}/followers?page=0&size=100`).pipe(
      switchMap(usernames => {
        if (!usernames || usernames.length === 0) return of([]);
        return this.fetchUsersByUsernames(usernames);
      })
    );
  }

  getFollowing(username: string): Observable<User[]> {
    return this.http.get<string[]>(`/api/follow/${username}/following?page=0&size=100`).pipe(
      switchMap(usernames => {
        if (!usernames || usernames.length === 0) return of([]);
        return this.fetchUsersByUsernames(usernames);
      })
    );
  }

  removeFollower(username: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/remove-follower/${username}`);
  }

  searchUsers(query: string): Observable<User[]> {
    return this.http.get<User[]>(`/api/search/users?query=${encodeURIComponent(query)}`);
  }

  fetchUsersByUsernames(usernames: string[]): Observable<User[]> {
    return this.http.post<User[]>(`${this.apiUrl}/batch-users`, usernames);
  }
}