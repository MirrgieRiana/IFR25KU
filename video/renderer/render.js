// =============================================================================
// render.js — 構成jsonl と HTML テンプレートから連番フレーム画像を撮る汎用レンダラーなのだぁ🌱
// -----------------------------------------------------------------------------
// このレンダラーは「サラセニア寸劇」のことは何も知らないのだぁ。知っているのは、たった 2 つの約束だけなのだぁ：
//
//   1. テンプレート HTML は、読み込みが終わったら window.__ready を true にすること。
//   2. テンプレート HTML は、window.applyFrame(cfg) を持つこと。
//        - cfg は構成jsonl の 1 行（＝1 フレーム分の設定オブジェクト）なのだぁ。
//        - applyFrame(cfg) は、その設定どおりに画面を組み立てて、
//          「この画面の見た目を一意に表す文字列（再利用キー）」を返すのだぁ。
//
// レンダラーは、構成jsonl を頭から 1 行ずつ applyFrame に渡して、1 行につき 1 枚の画像を撮るのだぁ。
// 連続する行の再利用キーが同じなら、撮り直さずに前のコマを使い回すのだぁ（変化の無い場面が速くなるのだぁ）。
//
// 使い方：
//   node render.js <template.html> <frames.jsonl> <outDir>
//     template.html … window.__ready と window.applyFrame(cfg) を持つ HTML
//                      （assets.js などの相対参照は、この HTML の場所を基準に解決されるのだぁ）
//     frames.jsonl  … 1 行 = 1 フレームの構成（JSON オブジェクト）を並べたファイル
//     outDir        … 連番画像 f_00000.png … を書き出す先（丸ごと作り直すのだぁ）
//
// 環境変数：
//   CHROMIUM_PATH  … Chrome/Chromium 実行ファイル（既定 /tmp/chromium＝setup_chromium.js の展開先）
//   VIDEO_WIDTH    … 画面幅（既定 1280）
//   VIDEO_HEIGHT   … 画面高さ（既定 720）
// =============================================================================
const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const templatePath = process.argv[2];
const framesPath = process.argv[3];
const outDir = process.argv[4];
if (!templatePath || !framesPath || !outDir) {
  console.error('usage: node render.js <template.html> <frames.jsonl> <outDir>');
  process.exit(1);
}
const W = parseInt(process.env.VIDEO_WIDTH || '1280', 10);
const H = parseInt(process.env.VIDEO_HEIGHT || '720', 10);

// 構成jsonl を 1 行 = 1 フレームとして読み込むのだぁ（空行は読み飛ばすのだぁ）
const frames = fs.readFileSync(framesPath, 'utf-8').split('\n')
  .map(s => s.trim()).filter(s => s.length > 0).map(s => JSON.parse(s));

fs.rmSync(outDir, { recursive: true, force: true });
fs.mkdirSync(outDir, { recursive: true });

const pad = n => String(n).padStart(5, '0');

(async () => {
  const browser = await puppeteer.launch({
    // Chromium/Chrome の実行ファイルパスなのだぁ。環境変数 CHROMIUM_PATH で上書きできるのだぁ🌱
    // 何も指定しなければ setup_chromium.js が展開した /tmp/chromium を使うのだぁ。
    executablePath: process.env.CHROMIUM_PATH || '/tmp/chromium', headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-dev-shm-usage', '--disable-gpu',
      '--use-gl=swiftshader', '--force-color-profile=srgb', '--hide-scrollbars', '--font-render-hinting=none',
      '--disable-features=Vulkan,UseDBus,AudioServiceOutOfProcess', '--no-zygote', '--single-process', '--disable-dbus'],
  });
  const page = await browser.newPage();
  await page.setViewport({ width: W, height: H, deviceScaleFactor: 1 });
  await page.goto('file://' + path.resolve(templatePath), { waitUntil: 'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  await page.waitForFunction('window.__ready===true', { timeout: 20000 });

  let prevKey = null, prevFile = null, shots = 0;
  for (let i = 0; i < frames.length; i++) {
    // 1 フレーム分の構成を画面に反映し、その見た目を一意に表す再利用キーを受け取るのだぁ
    const key = await page.evaluate((cfg) => String(window.applyFrame(cfg)), frames[i]);
    const file = path.join(outDir, `f_${pad(i)}.png`);
    if (key === prevKey && prevFile) {
      fs.copyFileSync(prevFile, file);
    } else {
      // レイアウト確定を待ってから撮るのだぁ
      await new Promise(r => setTimeout(r, 8));
      await page.screenshot({ path: file });
      shots++; prevKey = key; prevFile = file;
    }
    if (i % 150 === 0) process.stdout.write(`\r frame ${i}/${frames.length} shots=${shots}   `);
  }
  await browser.close();
  console.log(`\n done: ${frames.length} frames, ${shots} unique screenshots`);
})().catch(e => { console.error('ERR', (e.stack || e.message)); process.exit(1); });
