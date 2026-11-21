// SECURED PAYMENT DEMO - parameterized queries, output encoding, CSP, validation.
// Run with: node --experimental-sqlite server.js
const express = require('express');
const { DatabaseSync } = require('node:sqlite');
const db = new DatabaseSync(':memory:');
db.exec(`CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT);
         CREATE TABLE payments (id INTEGER PRIMARY KEY, user_id INTEGER, cardholder TEXT, amount REAL, note TEXT);
         INSERT INTO users (username, password) VALUES ('alice', 'winter2026'), ('bob', 'hunter2');`);

const app = express();
app.use(express.urlencoded({ extended: false }));

// MITIGATION: Content Security Policy - the browser refuses inline scripts,
// so injected markup cannot execute even if it reached the page.
app.use((req, res, next) => {
  res.setHeader('Content-Security-Policy', "default-src 'self'; script-src 'self'; object-src 'none'; base-uri 'self'");
  next();
});

// MITIGATION: every untrusted value is HTML-escaped before it is written into the page.
const esc = s => String(s).replace(/[&<>"']/g, ch =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));

app.get('/', (req, res) => res.send(`<h1>PayDemo (secured)</h1>
<form method="POST" action="/login"><input name="username"><input name="password" type="password"><button>Log in</button></form>
<form method="POST" action="/pay"><input name="user_id" placeholder="user id"><input name="cardholder"><input name="amount"><input name="note" placeholder="note"><button>Pay</button></form>
<p><a href="/payments">View all payments</a></p>`));

// MITIGATION: prepared statement with bound parameters - input can never change the SQL.
const loginStmt = db.prepare('SELECT * FROM users WHERE username = ? AND password = ?');
app.post('/login', (req, res) => {
  const row = loginStmt.get(String(req.body.username || ''), String(req.body.password || ''));
  // Same generic failure message either way: no user-enumeration hint.
  res.send(row ? 'Logged in as ' + esc(row.username) : 'Invalid username or password');
});

const payStmt = db.prepare('INSERT INTO payments (user_id, cardholder, amount, note) VALUES (?, ?, ?, ?)');
app.post('/pay', (req, res) => {
  // MITIGATION: whitelist validation before anything touches the database.
  const userId = Number(req.body.user_id);
  const amount = Number(req.body.amount);
  const cardholder = String(req.body.cardholder || '');
  const note = String(req.body.note || '');
  if (!Number.isInteger(userId) || !(amount > 0 && amount <= 10000) ||
      !/^[A-Za-z .'-]{1,60}$/.test(cardholder) || note.length > 200) {
    return res.status(400).send('Invalid payment details');
  }
  payStmt.run(userId, cardholder, amount, note);
  res.redirect('/payments');
});

app.get('/payments', (req, res) => {
  const rows = db.prepare('SELECT * FROM payments').all();
  // MITIGATION: values are escaped at render time.
  res.send('<h1>Payments</h1>' + rows.map(r =>
    `<p>${esc(r.cardholder)}: $${esc(r.amount)} - ${esc(r.note)}</p>`).join(''));
});

app.listen(4202, () => console.log('secured payment app on http://localhost:4202'));
