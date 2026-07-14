// PSD から、指定した ID のレイヤーを「全身キャンバスサイズ」の透過PNGとして書き出すのだぁ🌱
// ID体系は劇場の layers.json と同じ（兄弟の1始まりインデックスを - で連結、グループもインデックスを消費）。
const fs = require('fs');
const path = require('path');
const { readPsd, initializeCanvas } = require('ag-psd');
const { PNG } = require('pngjs');

// node-canvas を入れずに useImageData を使うため、createImageData だけをシムで供給するのだぁ🌱
initializeCanvas(
  (w, h) => { throw new Error('createCanvas should not be called in imageData mode'); },
  (w, h) => ({ width: w, height: h, data: new Uint8ClampedArray(w * h * 4) })
);

const target = process.argv[2];          // 'zundamon' or 'tsumugi'
const psdPath = process.argv[3];
const ids = process.argv[4].split(',');  // 抽出したいID
const outDir = process.argv[5];

const buf = fs.readFileSync(psdPath);
const psd = readPsd(buf, { useImageData: true, skipCompositeImageData: true, skipThumbnail: true });
const W = psd.width, H = psd.height;
console.log(`${target}: canvas ${W}x${H}`);

// id -> layer のマップを作るのだぁ
const map = {};
function walk(children, prefix) {
  children.forEach((node, i) => {
    const id = prefix ? `${prefix}-${i + 1}` : `${i + 1}`;
    if (node.children) walk(node.children, id);
    else map[id] = node;
  });
}
walk(psd.children, '');

fs.mkdirSync(outDir, { recursive: true });
for (const id of ids) {
  const ly = map[id];
  if (!ly) { console.log(`  MISSING id=${id}`); continue; }
  const png = new PNG({ width: W, height: H });
  png.data.fill(0);
  const idata = ly.imageData;
  if (idata) {
    const lw = idata.width, lh = idata.height;
    const ox = ly.left | 0, oy = ly.top | 0;
    for (let y = 0; y < lh; y++) {
      const dy = oy + y;
      if (dy < 0 || dy >= H) continue;
      for (let x = 0; x < lw; x++) {
        const dx = ox + x;
        if (dx < 0 || dx >= W) continue;
        const si = (y * lw + x) * 4, di = (dy * W + dx) * 4;
        png.data[di] = idata.data[si];
        png.data[di + 1] = idata.data[si + 1];
        png.data[di + 2] = idata.data[si + 2];
        png.data[di + 3] = idata.data[si + 3];
      }
    }
  }
  fs.writeFileSync(path.join(outDir, `${id}.png`), PNG.sync.write(png));
  console.log(`  ${id} <- ${ly.name} [${ly.left},${ly.top},${ly.right - ly.left}x${ly.bottom - ly.top}]`);
}
