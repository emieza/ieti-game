const { v4: uuidv4 } = require('uuid')
const express = require('express')
const http = require('http');
const WebSocket = require('ws')

const app = express()
const httpServer = http.createServer(app);
const wss = new WebSocket.Server({ server: httpServer })
const socketsClients = new Map()

const port = 3000

// HTTP ROUTES
app.use(express.static('public'))
app.get('/',root);


// WS client connections
wss.on('connection', (ws) => {
 console.log("Client connected")

 // Add client to the clients list
 const id = uuidv4()
 const color = Math.floor(Math.random() * 360)
 const metadata = { id, color }
 socketsClients.set(ws, metadata)
})

// What to do when a client is disconnected
wss.on("close", () => {
   socketsClients.delete(wss)
})

// WS messages
wss.on('message', (bufferedMessage) => {
   var messageAsString = bufferedMessage.toString()
   var messageAsObject = {}
  
   try { messageAsObject = JSON.parse(messageAsString) }
   catch (e) { console.log("Could not parse bufferedMessage from WS message") }

   if (messageAsObject.type == "bounce") {
     var rst = { type: "response", text: `Rebotar Websocket: '${messageAsObject.text}'` }
     ws.send(JSON.stringify(rst))
   } else if (messageAsObject.type == "broadcast") {
     var rst = { type: "response", text: `Broadcast Websocket: '${messageAsObject.text}'` }
     broadcast(rst)
   }
})


// SERVER START
httpServer.listen(port, appListen)
function appListen () {
 console.log(`Example app listening on: http://localhost:${port}`)
}

// HTTP
/////////////////////////////////
async function root(req,res) {
    res.send("Hello");
}



// WS : Web Sockets
/////////////////////////////////
async function broadcast (obj) {
 wss.clients.forEach((client) => {
   if (client.readyState === WebSocket.OPEN) {
     var messageAsString = JSON.stringify(obj)
     client.send(messageAsString)
   }
 })
}

