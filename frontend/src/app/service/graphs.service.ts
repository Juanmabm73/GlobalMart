import { Injectable } from "@angular/core";
import { environment } from "../enviroments/enviroment";
import { Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";


@Injectable({
providedIn: 'root'
})
export class GraphsService{

    private apiUrl = `${environment.apiUrl}/graphs`

    constructor(private http: HttpClient) {}

    getUserGraphData(): Observable<any> {
        return this.http.get(`${this.apiUrl}/userGraph`);
    }

    getCompanyGraphData(): Observable<any> {
        return this.http.get(`${this.apiUrl}/companyGraph`)
    }
}
