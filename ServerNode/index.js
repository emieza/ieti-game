const express = require('express')
const http = require('http');
const WebSocket = require('ws')

const app = express()
const httpServer = http.createServer(app);
const wss = new WebSocket.Server({ server: httpServer })

const { v4: uuidv4 } = require('uuid')
const port = 8888
var clients = {}

// HTTP ROUTES
app.use(express.static('public'))
app.get('/',root);


// WS client connections
wss.on('connection', function connection(ws) {
  var userid = uuidv4();
  console.log('Nova connexió: '+userid);
  clients[userid] = { "id":userid, "ws":ws, pos:{} };
  ws.send("Benvingut id="+userid);
  // TODO: crear totems i actualitzar

  ws.on('close', function close() {
    delete clients[userid];
    // TODO: esborrar totems?
  })

  ws.on('error', console.error);

  ws.on('message', function message(data) {
    try {
      var posData = JSON.parse(data);
      posData.id = userid;
      console.log('Pos data: %s', JSON.stringify(posData));
      // retransmetem posició a tothom
      broadcast(JSON.stringify(posData));
    } catch (e) {
      console.log("ERROR descodificant pos: "+e)
    }
  });

});


// SERVER START
httpServer.listen(port, appListen)
function appListen () {
 console.log(`Example app listening on: http://localhost:${port}`)
}

// HTTP
/////////////////////////////////
async function root(req,res) {
    res.send("IETI Game WebSocket Server");
}


// WS : Web Sockets
/////////////////////////////////
async function broadcast (obj) {
  for( var id in clients ) {
    var client = clients[id];
    //if (client.readyState === WebSocket.OPEN) {
      var messageAsString = JSON.stringify(obj)
      client.ws.send(obj);
    //}
  }
}

