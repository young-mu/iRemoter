var http = require('http');
var qs = require('querystring');
var url = require('url');

http.createServer(function(req, res) {
    if (req.method == 'POST') {
        var addr = req.connection.remoteAddress;
        var port = req.connection.remotePort;
        var pathname = url.parse(req.url).pathname;
        console.log('POST', pathname, 'request from', addr + ':' + port);
        if (pathname == '/ir-recv') {
            req.on('data', function(chunk) {
                var irData = "12345678";
                res.writeHead(200, {'content-type': 'text/plain'});
                res.end(irData);
            });
        }
        if (pathname == '/ir-send') {
            req.on('data', function(chunk) {
                var data = qs.parse(chunk.toString());
                res.writeHead(200, {'content-type': 'text/plain'});
                res.end("irCode send success");
            });
        }
    }
}).listen(3000);

console.log('server listen port 3000');
