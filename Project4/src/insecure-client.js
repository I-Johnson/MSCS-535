// INSECURE client: rejectUnauthorized=false disables certificate validation.
// This is the classic MITM-enabling mistake. Usage: node insecure-client.js <port>
const tls = require('tls');
const port = Number(process.argv[2] || 4301);
const sock = tls.connect({ host: 'localhost', port, rejectUnauthorized: false }, () => {
  console.log(`connected to port ${port}: authorized=${sock.authorized} (validation was OFF)`);
  sock.on('data', d => { process.stdout.write('received: ' + d); sock.end(); });
});
sock.on('error', e => console.log('error:', e.message));
