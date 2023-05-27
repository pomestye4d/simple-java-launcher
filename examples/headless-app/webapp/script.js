async function getVersion(){
    return await (await fetch("/api/version")).text()
}

async function waitForUpdate(){
    try {
        const version = await getVersion()
        if(window.version != version){
            window.location.reload();
            return;
        }
    } catch (e){
        //noops
    }
    window.setTimeout(waitForUpdate, 1000)
}
async function update(){
    window.document.getElementsByTagName("body")[0].innerHTML = "Updating application"
    await fetch("/api/update")
    window.setTimeout(waitForUpdate, 1000)
}

async function onLoad(){
    const version = await getVersion()
    window.document.getElementById("version-number").innerHTML = version
    window.version = version
    window.document.getElementById("update-button").onclick = async () =>{
        update()
    }
}

window.addEventListener("load", function(event) {
    onLoad();
});
