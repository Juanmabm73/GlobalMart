const loginButton = document.getElementsByClassName("login")[0];
const registerButton = document.getElementsByClassName("register")[0];
const popupContainer = document.getElementsByClassName("popup_container")[0];
const popupOverlay = document.getElementsByClassName("overlay")[0];
const popupTitle = document.getElementsByClassName("login-register_options_title")[0];
const popupLogin = document.getElementsByClassName("login_div")[0];
const popupRegister = document.getElementsByClassName("register_div")[0];

const popupContainerContent = document.getElementsByClassName("content")[0];
const popupContainerOverlay = document.getElementsByClassName("overlay")[0];

const popupButtonTexts = document.getElementsByClassName("button_text");

const popupButtonEmail = document.getElementsByClassName("email_login_button")[0];

const initialPopupButtons = Array.from(popupButtonTexts).map(btn => btn.innerHTML);

loginButton.addEventListener("click", function () {
    popupContainerContent.classList.add("active");
    popupContainerOverlay.classList.add("active");
    popupTitle.innerHTML = "Choose your login method";
    Array.from(popupButtonTexts).forEach((btn, index) => {
        btn.innerHTML = "Login " + initialPopupButtons[index];
    });
    popupButtonEmail.addEventListener("click", function () {
        popupContainerContent.classList.remove("active");
        popupRegister.classList.remove("active");
        popupLogin.classList.add("active");
    });
});

registerButton.addEventListener("click", function () {
    console.log("registerButton clicked");
    popupContainerContent.classList.add("active");
    popupContainerOverlay.classList.add("active");
    popupTitle.innerHTML = "Choose your register method";
    Array.from(popupButtonTexts).forEach((btn, index) => {
        btn.innerHTML = "Register " + initialPopupButtons[index];
    });
    popupButtonEmail.addEventListener("click", function () {
        popupLogin.classList.remove("active");
        popupContainerContent.classList.remove("active");
        popupRegister.classList.add("active");
    });
});

popupOverlay.addEventListener("click", function () {
    popupContainerContent.classList.remove("active");
    popupContainerOverlay.classList.remove("active");
    popupLogin.classList.remove("active");
    popupRegister.classList.remove("active");
    Array.from(popupButtonTexts).forEach((btn, index) => {
        btn.innerHTML = initialPopupButtons[index];
    });
});