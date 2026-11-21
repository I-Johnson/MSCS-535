// Genuine TLS server (port 4301). Presents the CA-signed localhost certificate.
const tls = require('tls'), fs = require('fs'), path = require('path');
const C = p => fs.readFileSync(path.join(__dirname, '..', 'certs', p));
tls.createServer({
  key: C('server-key.pem'), cert: C('server-cert.pem'),
  minVersion: 'TLSv1.2'
}, sock => {
  sock.write('SECRET-MESSAGE: account balance is 4200.00\n');
  sock.end();
}).listen(4301, () => console.log('genuine TLS server on 4301'));
