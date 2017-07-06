var mywebworker;

console.log("starting wurst");

window.onload = function () {
    console.log("loading wurst");
    mywebworker = new Worker('../build/gwt/out/de.peeeq.WurstCompiler/de.peeeq.WurstCompiler.nocache.js');
    console.log("worker loaded!!");
    mywebworker.addEventListener('message', function (e) {
        console.log('Worker said: ', e);
        var d = document.createElement('div');
        d.innerText = 'Worker generated: ' + e.data.payload;
        document.body.appendChild(d);
    }, false);

};

function callWebWorker() {
    console.log("Calling web worker...");
    mywebworker.postMessage('whatever');
}

var btn = document.getElementById("send-msg");
console.log("btn  =", btn);
btn.addEventListener("click", function (e) {
    callWebWorker();
});
