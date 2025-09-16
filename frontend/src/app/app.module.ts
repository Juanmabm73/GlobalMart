import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { routes } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { NavComponent } from './components/nav/nav.component';
import { FooterComponent } from './components/footer/footer.component';
import { SliderComponent } from './components/slider/slider.component';
import { ProductListComponent } from './components/product-list/product-list.component';
import { ProductDetailComponent } from './components/product-detail/product-detail.component';
import { ShoppingCartComponent } from './components/shopping-cart/shopping-cart.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { FormComponent } from './components/newProdFrom/form.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { ProductSearchComponent } from './components/search/search.component';
import { UserGraphComponent } from './components/user-graph/user-graph.component';
import { CompanyGraphComponent } from './components/company-graph/company-graph.component';
import { ProfileFormComponent } from './components/profile-form/profile-form.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    HomeComponent,
    NavComponent,
    FooterComponent,
    SliderComponent,
    ProductListComponent,
    ProductDetailComponent,
    ShoppingCartComponent,
    AboutUsComponent,
    FormComponent,
    UserProfileComponent,
    ProductSearchComponent,
    UserGraphComponent,
    CompanyGraphComponent,
    ProfileFormComponent

  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    CommonModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }