{
    "use strict";
    const logoutLink = document.querySelector('#logout-link');
    const logoutForm = document.querySelector('#logout-form');
    logoutLink.addEventListener('click', e => {
        e.preventDefault();
        logoutForm.submit();
    });

    let video = document.getElementById("myVideo");
    let btn = document.getElementById("myBtn");

    function myFunction() {
        if (video.paused) {
            video.play();
            btn.innerHTML = "Pause";
        } else {
            video.pause();
            btn.innerHTML = "Play";
        }
    }
}