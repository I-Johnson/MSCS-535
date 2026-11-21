// MITIGATED APP: fixes for the vulnerabilities in ../vulnerable/server.js
const express = require('express');
const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(express.json());

// (3) MITIGATION a - Content Security Policy: only same-origin scripts/styles may load,
// and inline scripts are not executed, so injected <script> payloads cannot run.
app.use((req, res, next) => {
  res.setHeader('Content-Security-Policy',
    "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self'; object-src 'none'; base-uri 'self'; frame-ancestors 'none'");
  res.setHeader('X-Content-Type-Options', 'nosniff');
  next();
});

// (3) MITIGATION b - output encoding: every untrusted value is HTML-escaped before render.
function esc(s) {
  return String(s).replace(/[&<>"']/g, ch =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));
}

const comments = [];

function page(body) {
  return `<!doctype html><html><head><title>Guestbook (secured)</title></head>
<body><h1>Guestbook</h1>
<form method="POST" action="/comment">
  <input name="author" placeholder="name" maxlength="40">
  <input name="text" placeholder="comment" maxlength="500">
  <button>Post</button>
</form>
<form method="GET" action="/search">
  <input name="q" placeholder="search comments"><button>Search</button>
</form>
<h2>Comments</h2>${body}</body></html>`;
}

app.get('/', (req, res) => {
  const list = comments.map(c => `<p><b>${esc(c.author)}</b>: ${esc(c.text)}</p>`).join('');
  res.send(page(list));
});
app.post('/comment', (req, res) => {
  const author = String(req.body.author || '').slice(0, 40);
  const text = String(req.body.text || '').slice(0, 500);
  comments.push({ author, text });
  res.redirect('/');
});

app.get('/search', (req, res) => {
  const q = String(req.query.q || '');
  const hits = comments.filter(c => c.text.includes(q)).map(c => `<p>${esc(c.text)}</p>`).join('');
  res.send(page(`<p>Results for: ${esc(q)}</p>${hits}`));
});

// (3) MITIGATION c - no eval(): the calculator parses a whitelist grammar
// (numbers, + - * / ( ) and whitespace) and evaluates it with a shunting-yard parser.
function safeCalc(expr) {
  const tokens = String(expr).match(/\d+(\.\d+)?|[+\-*/()]/g);
  if (!tokens || tokens.join('').replace(/\s+/g, '') !== String(expr).replace(/\s+/g, '')) {
    throw new Error('only numbers and + - * / ( ) are allowed');
  }
  const prec = { '+': 1, '-': 1, '*': 2, '/': 2 };
  const out = [], ops = [];
  for (const t of tokens) {
    if (/^\d/.test(t)) out.push(parseFloat(t));
    else if (t === '(') ops.push(t);
    else if (t === ')') {
      while (ops.length && ops[ops.length - 1] !== '(') out.push(ops.pop());
      ops.pop();
    } else {
      while (ops.length && prec[ops[ops.length - 1]] >= prec[t]) out.push(ops.pop());
      ops.push(t);
    }
  }
  while (ops.length) out.push(ops.pop());
  const st = [];
  for (const t of out) {
    if (typeof t === 'number') st.push(t);
    else {
      const b = st.pop(), a = st.pop();
      if (a === undefined || b === undefined) throw new Error('malformed expression');
      st.push(t === '+' ? a + b : t === '-' ? a - b : t === '*' ? a * b : a / b);
    }
  }
  if (st.length !== 1 || !isFinite(st[0])) throw new Error('malformed expression');
  return st[0];
}

app.get('/calc', (req, res) => {
  const expr = String(req.query.expr || '');
  let out;
  try { out = String(safeCalc(expr)); } catch (e) { out = 'Error: ' + e.message; }
  res.send(`<p>expr = ${esc(expr)}</p><p>result = ${esc(out)}</p>`);
});

app.listen(4102, () => console.log('secured app on http://localhost:4102'));
