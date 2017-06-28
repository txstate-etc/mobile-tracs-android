document.querySelector('form').onsubmit = function() {
    var username = document.querySelector('form input[name="username"]').value = document.querySelector('form input[name="username"]').value.toLowerCase().trim();
    var password = document.querySelector('form input[name="password"]').value;
    document.querySelector('form input[name="publicWorkstation"]').checked = false;
    TracsWebView.deliver(username, password);
    return true;
};
document.querySelector('form input[name="publicWorkstation"]').style.display = "none";
document.querySelector('form label[for="publicWorkstation"]').style.display = "none";
