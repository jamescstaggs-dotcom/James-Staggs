# Red Dirt Drain Inspection: OKC sewer camera website

A static site (HTML/CSS/JS, no build step). Open `index.html` in a browser, or deploy the folder to any static host (GitHub Pages, Netlify, Cloudflare Pages).

## What makes it different
- **Camera-monitor hero.** An animated sewer-camera feed with a live HUD that shows the footage counter, the incline and REC.
- **Ride-along pipe run.** Drag the camera along a sample 80 ft lateral to see each finding, its severity grade and the recommendation.
- **Year-built decoder.** Enter a home's build year to see the pipe material that was likely used in OKC at the time and the risks that come with it.
- **Local content.** It covers shrink-swell red clay, root-hungry trees, older OKC neighborhoods and metro service areas.

## Placeholders to replace before launch
- Business name "Red Dirt Drain Inspection", which appears in `index.html` (title, header, footer, JSON-LD)
- Phone `(405) 555-0142`, which appears several times in `index.html`
- Prices in the Services section
- The booking form only validates and shows a thank-you message. Connect it to Formspree, Netlify Forms or your CRM (see the TODO in `main.js`).
