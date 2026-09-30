// 構成 jsonl と HTML のテンプレートから、連番のフレーム画像をレンダリングする汎用のレンダラーなのだ～🌱
// 使い方と、テンプレートが満たすべき要件と、環境変数は、README.md に書いてあるのだ～🌱
const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');
const { pathToFileURL } = require('url');

const templatePath = process.argv[2];
const framesPath = process.argv[3];
const outDir = process.argv[4];
if (!templatePath || !framesPath || !outDir) {
  console.error('usage: node render.js <template.html> <frames.jsonl> <outDir>');
  process.exit(1);
}
const W = parseInt(process.env.VIDEO_WIDTH || '1280', 10);
const H = parseInt(process.env.VIDEO_HEIGHT || '720', 10);

// 構成 jsonl の 1 行を、1 フレームとして読み込むのだ～🌱
const frames = fs.readFileSync(framesPath, 'utf-8').split('\n')
  .map(s => s.trim()).filter(s => s.length > 0).map(s => JSON.parse(s));

if (fs.existsSync(outDir) && !(fs.statSync(outDir).isDirectory() && fs.readdirSync(outDir).length === 0)) {
  console.error(`error: outDir must be an empty directory or must not exist: ${outDir}`);
  process.exit(1);
}
fs.mkdirSync(outDir, { recursive: true });

const pad = n => String(n).padStart(5, '0');
const normalizeJson = v => Array.isArray(v) ? `[${v.map(normalizeJson).join(',')}]`
  : v !== null && typeof v === 'object' ? `{${Object.keys(v).sort().map(k => `${JSON.stringify(k)}:${normalizeJson(v[k])}`).join(',')}}`
  : JSON.stringify(v);

(async () => {
  const browser = await puppeteer.launch({
    executablePath: process.env.CHROMIUM_PATH || '/tmp/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage', '--disable-gpu',
      '--use-gl=swiftshader', '--force-color-profile=srgb', '--hide-scrollbars', '--font-render-hinting=none',
      '--disable-features=Vulkan,UseDBus,AudioServiceOutOfProcess', '--no-zygote', '--single-process', '--disable-dbus'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: W, height: H, deviceScaleFactor: 1 });
  await page.goto(pathToFileURL(path.resolve(templatePath)).href, { waitUntil: 'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  await page.waitForFunction('window.__ready===true', { timeout: 20000 });

  let prevKey = null, prevFile = null, shots = 0;
  for (let i = 0; i < frames.length; i++) {
    const key = normalizeJson(frames[i]);
    const file = path.join(outDir, `f_${pad(i)}.png`);
    if (key === prevKey && prevFile) {
      fs.copyFileSync(prevFile, file);
    } else {
      await page.evaluate((cfg) => { window.applyFrame(cfg); }, frames[i]);
      // レイアウトの確定を待ってから、スクリーンショットを撮るのだ～🌱
      await new Promise(r => setTimeout(r, 8));
      await page.screenshot({ path: file });
      shots++; prevKey = key; prevFile = file;
    }
    if (i % 150 === 0) process.stdout.write(`\r frame ${i}/${frames.length} shots=${shots}   `);
  }
  await browser.close();
  console.log(`\n done: ${frames.length} frames, ${shots} unique screenshots`);
})().catch(e => { console.error('ERR', (e.stack || e.message)); process.exit(1); });
