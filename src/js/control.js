var socket;

function connect(){

    socket = new WebSocket("ws://localhost:8080");

    socket.onmessage = function(event){

        var msg = JSON.parse(event.data);

        switch(msg.type){

            case "draw":
                document.getElementById('canvas').innerHTML += "\n" + msg.image;
                break;
            case "paintings":
                document.getElementById('userId').innerHTML = "";
                msg.paintings.forEach(function(item, index){
                    document.getElementById('paintings').innerHTML += item.id + " </br>";
                });
                break;
            case "users":
                document.getElementById('invitedUserId').innerHTML = "";
                msg.users.forEach(function(item, index){
                    document.getElementById('users').innerHTML += item.id + " : " + item.email +"</br>";
                });
                break;

            case "openPainting":
                //TODO...
                break;

            case "invite":
                //TODO...
                break;

            case "create":
                //TODO...
                break;
        }
    }
}

function quit() {
    if(socket!=null){
        socket.close();
        document.getElementById('status').innerHTML = "Paint Released";
    }
}

function draw(){
    var msg = {
        type:'draw',
        image:document.getElementById('canvas').value,
        id:document.getElementById('paintId').value,
        <!--current userId from session??-->
        user:document.getElementById('userId').value
    }
    socket.send(JSON.stringify(msg));
}

function getPaintings(){
    //get all paintings that a user created/complied before
    var msg ={
        type:'paintings',
        user:document.getElementById('userId').value
    }
    socket.send(JSON.stringify(msg));
}

function openPainting(){
    var msg = {
        type:'openPainting',
        id:document.getElementById('paintId').value
    }
    socket.send(JSON.stringify(msg));
}

function createPainting(){
    var msg = {
        type:'create',
        name:document.getElementById('paintName').value
    }
    socket.send(JSON.stringify(msg));
}

function getUsers(){
    var msg = {
        type:'users',
    }
    socket.send(JSON.stringify(msg));
}

function inviteUser() {
    var msg = {
        type:'invite',
        id:document.getElementById('invitedUserId').value
    }
    socket.send(JSON.stringify(msg));
}

