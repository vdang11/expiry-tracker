# Scan accuracy check

Measures how well `/api/vision/scan` reads expiry dates, using your own photos.

1. Put product photos in a folder and name each one `<anything>__<expected>.jpg`
   - `expected` is the real expiry date as `YYYY-MM-DD`, or `UNKNOWN` if the photo has no readable date
   - e.g. `vinamilk__2027-03-15.jpg`, `mi-goi-mat-sau__UNKNOWN.jpg`
2. Start the backend with an OpenAI key, then run:

```bash
API_URL=http://localhost:8080 EMAIL=you@example.com PASSWORD=yourpassword \
  node tools/scan-eval/scan-eval.mjs ~/expiry-photos
```

Output per photo, then a summary:

- **correct**: date matches
- **wrong but flagged**: the app returned `UNKNOWN` or asked the user to review, so the user is warned
- **silently wrong**: a wrong date that looks confident; this is the dangerous case (exit code 2)

Each photo costs one OpenAI vision call. Requires Node 18+.

Last run on 12 synthetic labels (blur, rotation, low contrast, dot-matrix,
NSX + HSD on the same label, several date formats, no date), 3 runs each:
11/12 correct every run, the blurry photo returned `UNKNOWN` with a
"take a clearer photo" message, 0 silently wrong. Synthetic labels are
easier than real packaging, so run this with real photos before trusting it.
