let slide_images = document.querySelectorAll('.slide_image');
let next = document.querySelector('.next');
let prev = document.querySelector('.prev');

let dots = document.querySelectorAll('.dot');

var counter = 0;

next.addEventListener('click', slide_next);
function slide_next() {
    slide_images[counter].style.animation = 'next1 0.5s ease-in forwards';

    counter++;
    if (counter == slide_images.length) {
        counter = 0;
    }

    slide_images[counter].style.animation = 'next2 0.5s ease-in forwards';
}

prev.addEventListener('click', slide_prev);
function slide_prev() {
    slide_images[counter].style.animation = 'prev1 0.5s ease-in forwards';

    counter--;
    if (counter < 0) {
        counter = slide_images.length - 1;
    }

    slide_images[counter].style.animation = 'prev2 0.5s ease-in forwards';
}

// ------------------------ Auto Sliding ------------------------
function auto_sliding() {
    delet_interval = setInterval(timer, 5000);
    function timer() {
        slide_next();
    }
}
auto_sliding();

// ------------------------ Stop/resume when mouse is over/out ------------------------
const slider = document.querySelector('.slider');
slider.addEventListener('mouseover', function () {
    clearInterval(delet_interval);
});


slider.addEventListener('mouseout', auto_sliding);