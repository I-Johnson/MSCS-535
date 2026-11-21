// SECURE client: validates the certificate chain against the trusted CA and
// verifies the hostname. A rogue certificate is rejected before any data flows.
// Usage: node secure-client.js <port>
const tls = require('tls'), fs = require('fs'), path = require('path');
const port = Number(process.argv[2] || 4301);
const ca = fs.readFileSync(path.join(__dirname, '..', 'certs', 'ca-cert.pem'));
const sock = tls.connect({
  host: 'localhost', port, ca,
  checkServerIdentity: tls.checkServerIdentity, // hostname verification (default)
  rejectUnauthorized: true,                     // never silently accept
  minVersion: 'TLSv1.2'
}, () => {
  console.log(`connected to port ${port}: authorized=${sock.authorized}`);
  console.log(`protocol=${sock.getProtocol()} cipher=${sock.getCipher().name}`);
  const cert = sock.getPeerCertificate();
  console.log(`peer cert subject=${cert.subject.CN} issuer=${cert.issuer.CN}`);
  sock.on('data', d => { process.stdout.write('received: ' + d); sock.end(); });
});
sock.on('error', e => console.log(`connection to port ${port} REJECTED: ${e.message}`));
