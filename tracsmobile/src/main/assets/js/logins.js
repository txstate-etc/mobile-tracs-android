document.getElementsByTagName('form')[0].onsubmit = function() {
    var username = document.getElementsByName('username')[0].value.toLowerCase().trim();
    var password = document.getElementsByName('password')[0].value;
    var publicStation = document.getElementsByName('publicWorkstation')[0].checked
    TracsWebView.deliver(username, password, !publicStation);
    return true;
}
