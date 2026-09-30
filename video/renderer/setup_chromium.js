const chromium = require('@sparticuz/chromium');
(async()=>{
  const p = await chromium.executablePath();  // /tmp/chromium に展開するのだ～🌱
  console.log('executablePath:', p);
  const fs=require('fs'); console.log('exists:', fs.existsSync(p));
})();
