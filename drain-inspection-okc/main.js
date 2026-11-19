// Red Dirt Drain Inspection — interactions
(() => {
  const $ = (s, r = document) => r.querySelector(s);
  const $$ = (s, r = document) => [...r.querySelectorAll(s)];
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  $('#year').textContent = new Date().getFullYear();

  /* ---------- Hero HUD: clock, distance counter, rotating notes ---------- */
  const clock = $('#hudClock'), dist = $('#hudDist'), incl = $('#hudIncl'), note = $('#hudNote');
  const notes = ['CLEANOUT → STREET', 'JOINT @ 18.0 FT', 'ROOT INTRUSION', 'BELLY · STANDING H2O', 'APPROACHING MAIN'];
  const start = Date.now();
  let ft = 0;
  const pad = n => String(n).padStart(2, '0');
  function tickHud() {
    const s = Math.floor((Date.now() - start) / 1000);
    clock.textContent = `${pad(Math.floor(s / 3600))}:${pad(Math.floor(s / 60) % 60)}:${pad(s % 60)}`;
    ft = (ft + 0.3) % 92;
    dist.textContent = ft.toFixed(1).padStart(5, '0');
    incl.textContent = (-1.2 + Math.sin(ft / 6) * 0.9).toFixed(1);
  }
  tickHud();
  if (!reduceMotion) {
    setInterval(tickHud, 250);
    let ni = 0;
    setInterval(() => {
      note.style.opacity = 0;
      setTimeout(() => { ni = (ni + 1) % notes.length; note.textContent = notes[ni]; note.style.opacity = 1; }, 300);
    }, 3200);
  }

  /* ---------- Interactive pipe run ---------- */
  const findings = [
    { ft: 0, grade: 0, title: 'Camera at the cleanout',
      body: 'Drag the slider or tap a marker to move the camera down the line.', rec: '' },
    { ft: 6, grade: 2, title: 'Cast iron tuberculation',
      body: 'The first few feet under the slab are original cast iron. Rust scale (tuberculation) narrows the pipe and catches paper. This is common in homes built before 1970.',
      rec: 'MONITOR · descale if slow drains continue' },
    { ft: 23.5, grade: 4, title: 'Root mass at a clay joint',
      body: 'Fine roots, probably from the elm out front, are coming in through a joint in the clay pipe and blocking about 40% of it. This is the most common serious finding we see in OKC.',
      rec: 'REPAIR · spot repair or line this section' },
    { ft: 41, grade: 3, title: 'Offset joint, about ¾ inch',
      body: 'Soil movement has shifted one pipe section away from the next. Waste can hang up on the lip it leaves behind, and over time the gap invites more roots.',
      rec: 'WATCH · include in any lining bid' },
    { ft: 55, grade: 3, title: 'Belly with standing water',
      body: 'Between 52 and 58 ft the line sags and holds about 30% water. That is classic shrink-swell clay damage. Solids settle here and the clogs keep coming back.',
      rec: 'EVALUATE · get a plumber’s opinion on re-sloping' },
    { ft: 78, grade: 1, title: 'Clean tie-in to the city main',
      body: 'The lateral connects to the public main cleanly with no defects at the tap. From here on, the pipe belongs to the city.',
      rec: 'NO ACTION' },
  ];
  const slider = $('#runSlider'), fill = $('#runFill'), cam = $('#runCam');
  const markers = $$('.marker');
  const fFt = $('#fFt'), fGrade = $('#fGrade'), fTitle = $('#fTitle'), fBody = $('#fBody'), fRec = $('#fRec');
  let shown = null;

  function nearestFinding(v) {
    let best = findings[0];
    for (const f of findings) if (Math.abs(f.ft - v) <= 4) best = f;
    return best;
  }
  function renderRun(v) {
    const pct = (v / 80) * 100;
    fill.style.width = pct + '%';
    cam.style.left = pct + '%';
    fFt.textContent = Number(v).toFixed(1) + ' FT';
    const f = nearestFinding(v);
    if (f !== shown) {
      shown = f;
      fGrade.dataset.g = f.grade;
      fGrade.textContent = f.grade ? `Grade ${f.grade}` : 'Grade –';
      fTitle.textContent = f.title;
      fBody.textContent = f.body;
      fRec.textContent = f.rec;
      markers.forEach(m => m.classList.toggle('is-active', Number(m.dataset.ft) === f.ft && f.ft !== 0));
    }
  }
  slider.addEventListener('input', e => renderRun(Number(e.target.value)));
  markers.forEach(m => m.addEventListener('click', () => {
    slider.value = m.dataset.ft;
    renderRun(Number(m.dataset.ft));
  }));
  renderRun(0);

  /* ---------- Year-built decoder ---------- */
  const eras = [
    { from: 1889, to: 1945, label: 'Land Run to WWII', risk: 90,
      material: 'Vitrified clay tile (VCP) with mortar or bare joints, often with cast iron inside the house',
      issues: ['Joints every 2–3 ft give roots many ways in', 'Cracked or broken bells from decades of clay movement', 'Earlier repairs in other materials mixed in'],
      tip: 'This is the most root-prone era. If the house has big old trees, scope it before you buy. No exceptions.' },
    { from: 1946, to: 1972, label: 'Post-war boom', risk: 80,
      material: 'Clay tile laterals, cast iron under the slab, and in some homes Orangeburg (tar paper) pipe',
      issues: ['Orangeburg can blister and flatten into an oval', 'Cast iron under the slab scales up and can split along the bottom', 'Bellies from decades of shrink-swell cycles'],
      tip: 'Lots of OKC ranch homes from this era are on their original lines. Ask whether the line has ever been replaced and get the receipt.' },
    { from: 1973, to: 1985, label: 'Transition years', risk: 55,
      material: 'Early PVC or ABS plastic, sometimes tied into older clay sections out at the street',
      issues: ['Glued joints from early installs can separate', 'Bad bedding causes sags in clay soil', 'Old clay pipe may remain where the line meets the main'],
      tip: 'The line under the house may be fine while the last 20 feet to the street is still old clay. The camera shows you where one ends and the other begins.' },
    { from: 1986, to: 2005, label: 'Modern PVC', risk: 35,
      material: 'Schedule 40 PVC with solvent-welded joints',
      issues: ['Bellies from poor compaction during construction', 'Construction debris left in the line', 'Damage from later sprinkler or fence installs'],
      tip: 'PVC resists roots well. Problems here usually come from how the line was installed, not from the pipe itself.' },
    { from: 2006, to: 2026, label: 'Recent build', risk: 20,
      material: 'PVC (SDR-35 or Schedule 40) laid to current code',
      issues: ['Builder debris such as concrete, grout or rags', 'Settlement in new backfill during the first few years', 'Cuts from utility, fiber or irrigation contractors'],
      tip: 'Scoping before your one-year builder warranty runs out is a cheap way to catch a construction defect while someone else still pays for it.' },
  ];
  const out = $('#decoderOut'), yearInput = $('#yearInput');

  function decode(year) {
    const e = eras.find(x => year >= x.from && year <= x.to);
    if (!e) {
      out.innerHTML = '<p class="decoder-empty">Enter a year between 1889 (the Land Run) and 2026.</p>';
      return;
    }
    out.innerHTML = `
      <span class="era-tag">${e.from}–${e.to} · ${e.label}</span>
      <h3>Built in ${year}</h3>
      <p><strong>Likely pipe:</strong> ${e.material}</p>
      <div class="risk"><span>Risk</span><div class="risk-bar"><span style="width:0"></span></div><span>${e.risk >= 75 ? 'High' : e.risk >= 45 ? 'Moderate' : 'Lower'}</span></div>
      <ul>${e.issues.map(i => `<li>${i}</li>`).join('')}</ul>
      <p class="tip">${e.tip}</p>`;
    requestAnimationFrame(() => requestAnimationFrame(() => {
      out.querySelector('.risk-bar span').style.width = e.risk + '%';
    }));
  }
  $('#decoderForm').addEventListener('submit', ev => {
    ev.preventDefault();
    decode(parseInt(yearInput.value, 10));
  });
  $$('.era-chips button').forEach(b => b.addEventListener('click', () => {
    yearInput.value = b.dataset.year;
    decode(Number(b.dataset.year));
  }));

  /* ---------- Booking form (front-end only) ---------- */
  // TODO: connect to a form backend (Formspree, Netlify Forms, or your CRM) and replace the
  // simulated success below with a real POST.
  const form = $('#bookForm'), status = $('#formStatus');
  form.addEventListener('submit', ev => {
    ev.preventDefault();
    const required = $$('[required]', form);
    let ok = true;
    required.forEach(el => {
      const bad = !el.value.trim();
      el.classList.toggle('invalid', bad);
      if (bad) ok = false;
    });
    if (!ok) {
      status.className = 'form-status field-wide err';
      status.textContent = 'Please add your name and phone number so we can call you back.';
      return;
    }
    status.className = 'form-status field-wide ok';
    status.textContent = `Thanks, ${$('#fName').value.trim().split(' ')[0]}! We'll call you within one business hour to set up your inspection.`;
    form.reset();
  });
})();
