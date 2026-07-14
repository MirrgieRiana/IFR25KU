const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const FPS = 30;
const B = __dirname;
const TL = JSON.parse(fs.readFileSync(path.join(B,'timeline.json'),'utf-8'));
const TOTAL = TL.total;
const FRAMES = Math.ceil(TOTAL*FPS);
const outDir = path.join(B,'frames');
fs.rmSync(outDir,{recursive:true,force:true}); fs.mkdirSync(outDir,{recursive:true});

const pad = n => String(n).padStart(5,'0');

(async () => {
  const browser = await puppeteer.launch({
    // Chromium/Chrome の実行ファイルパスなのだぁ。環境変数 CHROMIUM_PATH で上書きできるのだぁ🌱
    // 何も指定しなければ setup_chromium.js が展開した /tmp/chromium を使うのだぁ。
    executablePath: process.env.CHROMIUM_PATH || '/tmp/chromium', headless: 'new',
    args: ['--no-sandbox','--disable-setuid-sandbox','--disable-dev-shm-usage','--disable-gpu',
      '--use-gl=swiftshader','--force-color-profile=srgb','--hide-scrollbars','--font-render-hinting=none',
      '--disable-features=Vulkan,UseDBus,AudioServiceOutOfProcess','--no-zygote','--single-process','--disable-dbus'],
  });
  const page = await browser.newPage();
  await page.setViewport({width:1280,height:720,deviceScaleFactor:1});
  await page.goto('file://'+path.join(B,'scene.html'),{waitUntil:'load'});
  await page.evaluate(async()=>{ await document.fonts.ready; });
  await page.waitForFunction('window.__ready===true',{timeout:20000});

  let prevKey=null, prevFile=null, shots=0;
  for(let i=0;i<FRAMES;i++){
    const t = i/FPS;
    const key = await page.evaluate((t)=>{
      window.seek(t);
      const op = s => (+getComputedStyle(document.querySelector(s)).opacity).toFixed(2);
      return [op('#thumbTop'),op('#bgBog'),op('#itemPlant'),op('#itemLeaf'),
              op('#subwrap'),op('#credit'),op('#band'),window.__lastSubKey||'',window.__tachieKey||'',window.__poseKey||''].join(',');
    }, t);
    const file = path.join(outDir,`f_${pad(i)}.png`);
    if(key===prevKey && prevFile){
      fs.copyFileSync(prevFile,file);
    } else {
      // レイアウト確定を待ってから撮るのだぁ
      await new Promise(r=>setTimeout(r,8));
      await page.screenshot({path:file});
      shots++; prevKey=key; prevFile=file;
    }
    if(i%150===0) process.stdout.write(`\r frame ${i}/${FRAMES} shots=${shots}   `);
  }
  await browser.close();
  console.log(`\n done: ${FRAMES} frames, ${shots} unique screenshots`);
})().catch(e=>{console.error('ERR',(e.stack||e.message)); process.exit(1)});
