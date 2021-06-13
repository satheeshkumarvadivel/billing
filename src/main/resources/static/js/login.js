$(document).ready(function () {
    localStorage.setItem('tt', 'TUFTVjpWRVNBNzI2Nw==');
    localStorage.setItem('a', null);
    $('#loginButton').click(check);
});

function check() {
    let username = $('#username').val();
    let password = $('#password').val();
    if (btoa(username + ":" + password) == localStorage.getItem('tt')) {
        localStorage.setItem('a', 's');
        window.location.href = "/";
    } else {
        alert("Invalid Username or Password");
    }
}