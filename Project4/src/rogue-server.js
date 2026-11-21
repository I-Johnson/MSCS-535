// Simulated man-in-the-middle (port 4302). Presents a self-made "localhost"
// certificate from the attacker's own CA. A real MITM would sit on the path;
// here it just occupies a different port.
const tls = require('tls'), fs = require('fs'), path = require('path');
const C = p => fs.readFileSync(path.join(__dirname, '..', 'certs', p));
tls.createServer({
  key: C('rogue-key.pem'), cert: C('rogue-cert.pem'),
  minVersion: 'TLSv1.2'
}, sock => {
  sock.write('INTERCEPTED-FAKE: account balance is 0.00\n');
  sock.end();
}).listen(4302, () => console.log('rogue TLS server on 4302'));
