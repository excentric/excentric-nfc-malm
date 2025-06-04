import notifier from 'node-notifier';

function desktopNotify(title: string, message: string) {
    notifier.notify({
        title: title,
        message: message,
        sound: false,
        wait: false,
        timeout: 1,
    });
}

console.log("not")

desktopNotify("hello", "world")
