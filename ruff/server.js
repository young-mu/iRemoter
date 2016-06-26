var http = require('http');
var qs = require('querystring');
var url = require('url');

http.createServer(function(req, res) {
    if (req.method == 'POST') {
        console.log('POST request from', req.headers.host);
        var pathname = url.parse(req.url).pathname;
        if (pathname == '/ir-post') {
            req.on('data', function(chunk) {
                data = qs.parse(chunk.toString());
                res.writeHead(200, {'content-type': 'text/plain'});
                res.end('irDevice = ' + data.irDevice + ', irCode = ' + data.irCode);
            });
        }
    }
}).listen(3000);

console.log('server listen port 3000');
