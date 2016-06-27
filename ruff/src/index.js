'use strict';

var http = require('http');
var qs = require('querystring');
var url = require('url');


$.ready(function (error) {
    if (error) {
        console.log(error);
        return;
    }

    $('#led-r').turnOn();

    http.createServer(function(req, res) {
        if (req.method == 'POST') {
            var addr = req.connection.remoteAddress;
            var port = req.connection.remotePort;
            console.log('POST request from', addr + ':' + port);
            var pathname = url.parse(req.url).pathname;
            if (pathname == '/ir-recv') {
                req.on('data', function(chunk) {
                    $('#irr-01').on('data', function(data) {
                        res.writeHead(200, {'content-type': 'text/plain'});
                        res.end(data.toString());
                    });
                });
            }
            if (pathname == '/ir-send') {
                req.on('data', function(chunk) {
                    var data = qs.parse(chunk.toString());
                    $('#irt-01').send(eval('['+data.irCode+']'), function() {
                        if (error) {
                            console.log(error);
                            res.writeHead(200, {'content-type': 'text/plain'});
                            res.end("irCode send failed");
                        } else {
                            res.writeHead(200, {'content-type': 'text/plain'});
                            res.end("irCode send success");
                        }
                    });
                });
            }
        }
    }).listen(3000);

    console.log('server listen port 3000');

});

$.end(function () {
    $('#led-r').turnOff();
});
