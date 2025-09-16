import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class ApiConfigService {
    private apiBaseUrl = 'https://localhost:443/api/v1';

    getApiBaseUrl(): string {
        return this.apiBaseUrl;
    }
}