// VULNERABLE DEMO APP - intentionally insecure. For coursework demonstration only.
const express = require('express');
const app = express();
app.use(express.urlencoded({ extended: false }));
app.use(express.json());

// In-memory "guestbook" store
const comments = [];

function page(body) {
  return `<!doctype html><html><head><title>Guestbook (vulnerable)</title></head>
<body><h1>Guestbook</h1>
<form method="POST" action="/comment">
  <input name="author" placeholder="name">
  <input name="text" placeholder="comment">
  <button>Post</button>
</form>
<form method="GET" action="/search">
  <input name="q" placeholder="search comments"><button>Search</button>
</form>
<h2>Comments</h2>${body}</body></html>`;
}

// (1) STORED XSS: comment text is rendered into the page without any encoding.
app.get('/', (req, res) => {
  const list = comments.map(c => `<p><b>${c.author}</b>: ${c.text}</p>`).join('');
  res.send(page(list));
});
app.post('/comment', (req, res) => {
  comments.push({ author: req.body.author || '', text: req.body.text || '' });
  res.redirect('/');
});

// (1) REFLECTED XSS: the query string is echoed into the page without encoding.
app.get('/search', (req, res) => {
  const q = req.query.q || '';
  const hits = comments.filter(c => c.text.includes(q)).map(c => `<p>${c.text}</p>`).join('');
  res.send(page(`<p>Results for: ${q}</p>${hits}`));
});

// (2) DYNAMIC CODE EVALUATION: a "calculator" that passes user input to eval().
app.get('/calc', (req, res) => {
  const expr = req.query.expr || '';
  let out;
  try { out = String(eval(expr)); } catch (e) { out = 'Error: ' + e.message; }
  res.send(`<p>expr = ${expr}</p><p>result = ${out}</p>`);
});

app.listen(4101, () => console.log('vulnerable app on http://localhost:4101'));
