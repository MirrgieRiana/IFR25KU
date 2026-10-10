// 構成 jsonl と HTML のテンプレートから、連番のフレーム画像をレンダリングする汎用のレンダラーなのだ～🌱
// 使い方と、テンプレートが満たすべき要件と、環境変数は、README.md に書いてあるのだ～🌱
const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');
const { pathToFileURL } = require('url');

const framesPath = process.argv[2];
const outDir = process.argv[3];
if (!framesPath || !outDir) {
  console.error('usage: node render.js <frames.jsonl> <outDir>');
  process.exit(1);
}
const W = parseInt(process.env.VIDEO_WIDTH || '1280', 10);
const H = parseInt(process.env.VIDEO_HEIGHT || '720', 10);

// 構成 jsonl の 1 行を、1 フレームとして読み込むのだ～🌱
let frames;
try {
  frames = fs.readFileSync(framesPath, 'utf-8').split('\n')
    .map((s, i) => ({ s: s.trim(), lineNumber: i + 1 })).filter(({ s }) => s.length > 0)
    .map(({ s, lineNumber }) => {
      let frame;
      try {
        frame = JSON.parse(s);
      } catch (e) {
        throw new Error(`invalid JSON at line ${lineNumber}: ${e.message}`);
      }
      if (frame === null || typeof frame !== 'object' || Array.isArray(frame) || typeof frame.template !== 'string') {
        throw new Error(`template must be a string in the root object at line ${lineNumber}`);
      }
      return frame;
    });
} catch (e) {
  console.error(`error: cannot read or parse frames: ${framesPath}: ${e.message}`);
  process.exit(1);
}

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

  // テンプレートの読み込み直しと、applyFrame が書き換える DOM が少なくなるように、テンプレート、正規化した JSON の順に並べ替えてから撮るのだ～🌱
  const jobs = frames.map((frame, i) => {
    // URL では空白が %20 になるから、解決したパスの辞書順で並べるには、パスそのもので比べる必要があるのだ～🌱
    const templatePath = path.resolve(path.dirname(framesPath), frame.template);
    return { i, templatePath, templateUrl: pathToFileURL(templatePath).href, key: normalizeJson(frame) };
  });
  const compare = (a, b) => a < b ? -1 : a > b ? 1 : 0;
  jobs.sort((a, b) => compare(a.templatePath, b.templatePath) || compare(a.key, b.key));

  let loadedTemplateUrl = null, prevKey = null, prevFile = null, shots = 0;
  for (let n = 0; n < jobs.length; n++) {
    const { i, templateUrl, key } = jobs[n];
    const file = path.join(outDir, `f_${pad(i)}.png`);
    if (key === prevKey && prevFile) {
      fs.copyFileSync(prevFile, file);
    } else {
      if (templateUrl !== loadedTemplateUrl) {
        await page.goto(templateUrl, { waitUntil: 'load' });
        await page.evaluate(async () => { await document.fonts.ready; });
        await page.waitForFunction('window.__ready===true', { timeout: 20000 });
        loadedTemplateUrl = templateUrl;
      }
      await page.evaluate(async (frame) => {
        await window.applyFrame(frame);
        // 書き換えた DOM が描画されてから、スクリーンショットを撮るのだ～🌱
        await new Promise(r => requestAnimationFrame(() => requestAnimationFrame(r)));
      }, frames[i]);
      await page.screenshot({ path: file });
      shots++;
      prevKey = key;
      prevFile = file;
    }
    if (n % 150 === 0) process.stdout.write(`\r frame ${n}/${frames.length} shots=${shots}   `);
  }
  await browser.close();
  console.log(`\n done: ${frames.length} frames, ${shots} unique screenshots`);
})().catch(e => { console.error('error:', (e.stack || e.message)); process.exit(1); });
