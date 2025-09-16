import { Component, OnInit, OnDestroy } from '@angular/core';

interface Slide {
  imageUrl: string;
  alt: string;
  animation?: string;
}

@Component({
  selector: 'app-slider',
  templateUrl: './slider.component.html',
  styleUrls: ['./slider.component.css'],
  standalone: false,
})
export class SliderComponent implements OnInit, OnDestroy {
  slides: Slide[] = [
    { imageUrl: '../../images/rebajas.png', alt: 'Slider 1' },
    { imageUrl: '../../images/rebajas2.png', alt: 'Slider 2' },
    { imageUrl: '../../images/rebajas3.png', alt: 'Slider 3' }
  ];

  currentSlide = 0;
  private intervalId: any;

  ngOnInit() {
    this.startAutoSlide();
  }

  ngOnDestroy() {
    this.stopAutoSlide();
  }

  slideNext() {
    this.slides[this.currentSlide].animation = 'next1 0.5s ease-in forwards';

    this.currentSlide++;
    if (this.currentSlide >= this.slides.length) {
      this.currentSlide = 0;
    }

    this.slides[this.currentSlide].animation = 'next2 0.5s ease-in forwards';
  }

  slidePrev() {
    this.slides[this.currentSlide].animation = 'prev1 0.5s ease-in forwards';

    this.currentSlide--;
    if (this.currentSlide < 0) {
      this.currentSlide = this.slides.length - 1;
    }

    this.slides[this.currentSlide].animation = 'prev2 0.5s ease-in forwards';
  }

  startAutoSlide() {
    this.intervalId = setInterval(() => {
      this.slideNext();
    }, 5000);
  }

  stopAutoSlide() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }
}