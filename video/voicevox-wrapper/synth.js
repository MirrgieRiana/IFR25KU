// 合成の依頼を並べた jsonl から、VOICEVOX ENGINE で 1 行ずつ音声を合成する、動画に依存しない汎用のラッパーなのだ～🌱
// 使い方と、依頼の行の形と、環境変数は、README.md に書いてあるのだ～🌱
const fs = require('fs');
const path = require('path');

const requestsPath = process.argv[2];
const outDir = process.argv[3];
if (!requestsPath || !outDir) {
  console.error('usage: node synth.js <requests.jsonl> <outDir>');
  process.exit(1);
}
const host = process.env.VOICEVOX_HOST || 'http://127.0.0.1:50021';

const isObject = v => v !== null && typeof v === 'object' && !Array.isArray(v);

// 依頼の jsonl の 1 行を、1 個の音声の依頼として読み込むのだ～🌱
let requests;
try {
  requests = fs.readFileSync(requestsPath, 'utf-8').split('\n')
    .map((s, i) => ({ s: s.trim(), lineNumber: i + 1 })).filter(({ s }) => s.length > 0)
    .map(({ s, lineNumber }) => {
      let request;
      try {
        request = JSON.parse(s);
      } catch (e) {
        throw new Error(`invalid JSON at line ${lineNumber}: ${e.message}`);
      }
      if (!isObject(request)) throw new Error(`the root must be an object at line ${lineNumber}`);
      if (!Number.isInteger(request.speaker)) throw new Error(`speaker must be an integer at line ${lineNumber}`);
      if (typeof request.text !== 'string') throw new Error(`text must be a string at line ${lineNumber}`);
      if (request.kana !== undefined && typeof request.kana !== 'string') throw new Error(`kana must be a string at line ${lineNumber}`);
      if (request.query !== undefined && !isObject(request.query)) throw new Error(`query must be an object at line ${lineNumber}`);
      return request;
    });
} catch (e) {
  console.error(`error: cannot read requests: ${requestsPath}: ${e.message}`);
  process.exit(1);
}

if (fs.existsSync(outDir) && !(fs.statSync(outDir).isDirectory() && fs.readdirSync(outDir).length === 0)) {
  console.error(`error: outDir must be an empty directory or must not exist: ${outDir}`);
  process.exit(1);
}
fs.mkdirSync(outDir, { recursive: true });

const pad = n => String(n).padStart(5, '0');

const post = async (endpoint, params, body) => {
  const response = await fetch(`${host}${endpoint}?${new URLSearchParams(params)}`, {
    method: 'POST',
    headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (!response.ok) throw new Error(`${endpoint} failed: HTTP ${response.status}: ${await response.text()}`);
  return response;
};

(async () => {
  for (let i = 0; i < requests.length; i++) {
    const { speaker, text, kana, query: overrides } = requests[i];
    const query = await (await post('/audio_query', { speaker, text })).json();
    if (kana !== undefined) {
      query.accent_phrases = await (await post('/accent_phrases', { speaker, text: kana, is_kana: 'true' })).json();
      query.kana = kana;
    }
    Object.assign(query, overrides);
    const wav = Buffer.from(await (await post('/synthesis', { speaker }, query)).arrayBuffer());
    fs.writeFileSync(path.join(outDir, `${pad(i)}.wav`), wav);
    fs.writeFileSync(path.join(outDir, `${pad(i)}.json`), `${JSON.stringify(query)}\n`);
    console.log(` ${pad(i)} speaker=${speaker} kana=${query.kana}`);
  }
  console.log(` done: ${requests.length} voices`);
})().catch(e => { console.error('error:', (e.stack || e.message)); process.exit(1); });
