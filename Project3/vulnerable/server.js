// VULNERABLE PAYMENT DEMO - intentionally insecure. For coursework demonstration only.
// Run with: node --experimental-sqlite server.js
const express = require('express');
const { DatabaseSync } = require('node:sqlite');
const db = new DatabaseSync(':memory:');
db.exec(`CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, password TEXT);
         CREATE TABLE payments (id INTEGER PRIMARY KEY, user_id INTEGER, cardholder TEXT, amount REAL, note TEXT);
         INSERT INTO users (username, password) VALUES ('alice', 'winter2026'), ('bob', 'hunter2');`);

const app = express();
app.use(express.urlencoded({ extended: false }));

app.get('/', (req, res) => res.send(`<h1>PayDemo (vulnerable)</h1>
<form method="POST" action="/login"><input name="username"><input name="password" type="password"><button>Log in</button></form>
<form method="POST" action="/pay"><input name="user_id" placeholder="user id"><input name="cardholder"><input name="amount"><input name="note" placeholder="note"><button>Pay</button></form>
<p><a href="/payments">View all payments</a></p>`));

// SQL INJECTION: the username is concatenated straight into the SQL string.
app.post('/login', (req, res) => {
  const sql = `SELECT * FROM users WHERE username = '${req.body.username}' AND password = '${req.body.password}'`;
  let row;
  try { row = db.prepare(sql).get(); } catch (e) { return res.send('SQL error: ' + e.message + '<br>executed: ' + sql); }
  res.send(row
    ? 'Logged in as ' + row.username + ' (user id ' + row.id + ')<br>executed: ' + sql
    : 'Login failed<br>executed: ' + sql);
});

// SQL INJECTION on insert, and STORED XSS: the note is saved and later rendered raw.
app.post('/pay', (req, res) => {
  const sql = `INSERT INTO payments (user_id, cardholder, amount, note) VALUES (${req.body.user_id}, '${req.body.cardholder}', ${req.body.amount}, '${req.body.note}')`;
  try { db.exec(sql); } catch (e) { return res.send('SQL error: ' + e.message); }
  res.redirect('/payments');
});

app.get('/payments', (req, res) => {
  const rows = db.prepare('SELECT * FROM payments').all();
  res.send('<h1>Payments</h1>' + rows.map(r => `<p>${r.cardholder}: $${r.amount} - ${r.note}</p>`).join(''));
});

app.listen(4201, () => console.log('vulnerable payment app on http://localhost:4201'));
