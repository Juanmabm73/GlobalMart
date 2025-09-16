import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { HomeComponent } from './components/home/home.component';
import { ShoppingCartComponent } from './components/shopping-cart/shopping-cart.component';
import { AuthGuard } from './guard/auth.guard';
import { ProductListComponent } from './components/product-list/product-list.component';
import { AboutUsComponent } from './components/about-us/about-us.component';
import { ProductDetailComponent } from './components/product-detail/product-detail.component';
import { FormComponent } from './components/newProdFrom/form.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';
import { UserGraphComponent } from './components/user-graph/user-graph.component';
import { AdminComponent } from './components/admin/admin.component';
import { CompanyGraphComponent } from './components/company-graph/company-graph.component';
import { ProductSearchComponent } from './components/search/search.component';
import { ProfileFormComponent } from './components/profile-form/profile-form.component';



export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'about-us', component: AboutUsComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'products/category/:category', component: ProductListComponent },
  { path: 'newProduct', component: FormComponent, canActivate: [AuthGuard], data: { requiredRoles: ['COMPANY', 'ADMIN'] } }, // Múltiples roles permitidos
  { path: 'user-profile', component: UserProfileComponent },
  { path: 'user-graph', component: UserGraphComponent, canActivate: [AuthGuard], data: { requiredRoles: ['USER'] } },
  { path: 'company-graph', component: CompanyGraphComponent, canActivate: [AuthGuard], data: { requiredRoles: ['COMPANY'] } },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard], data: { requiredRoles: ['ADMIN'] } },
  { path: 'search', component: ProductSearchComponent },
  { path: 'edit-profile', component: ProfileFormComponent },
  { path: 'shoppingcart', component: ShoppingCartComponent, canActivate: [AuthGuard], data: { requiredRoles: ['USER'] } },
  { path: '**', redirectTo: '' }
];