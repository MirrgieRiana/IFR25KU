// 依存のパッケージに含まれる Chromium を、/tmp/chromium に展開するのだ～🌱
const chromium = require('@sparticuz/chromium');
const fs = require('fs');

(async () => {
  const p = await chromium.executablePath();
  console.log('executablePath:', p);
  if (!fs.existsSync(p)) {
    console.error(`error: Chromium was not extracted: ${p}`);
    process.exit(1);
  }
})().catch(e => { console.error('error:', (e.stack || e.message)); process.exit(1); });
