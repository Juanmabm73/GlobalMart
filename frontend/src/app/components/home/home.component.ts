import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../../service/product.service';
import { RouterModule } from '@angular/router';
@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrls: ["./home.component.css"],
  standalone: false
})

export class HomeComponent implements OnInit{
  
  lastProducts: any[]= [];
  mostViewedProducts: any[]= [];
  
  constructor(
    private route: ActivatedRoute,
    private productService: ProductService
  ) { }
  

  ngOnInit(): void {
    this.loadLastProducts();
    this.loadMostViewedProducts();
  }
  
  
  loadMostViewedProducts(): void {
    this.productService.getMostViewedProducts().subscribe({
      next: (products) => this.mostViewedProducts = products,
      error: (err) => console.error('Error al cargar los productos más vistos', err)
    });
  }
  
  loadLastProducts(): void {
    this.productService.getLastProducts().subscribe({
      next: (products) => this.lastProducts = products,
      error: (err) => console.error('Error al cargar los últimos productos', err)
    });
  }
  
}


