# cloud-itonami.github.io — the fleet catalog

The org's root Pages site: <https://cloud-itonami.github.io/> — a
static catalog of every implemented governed-actor business, generated
from [`kotoba-lang/industry`](https://github.com/kotoba-lang/industry)'s
registry (the fleet's machine-readable source of truth), so the page
can never drift from the registry. Same zero-build pattern as the
venture demo pages: nbb authoring (`web/generate.cljs`), scittle
in-browser filtering (`web/catalog.cljs`), headless harness
(`web/verify_catalog.cljs`).

```bash
cd web && ../../../../node_modules/.bin/nbb \
  --classpath "../../../kotoba-lang/html/src:../../../kotoba-lang/css/src" \
  generate.cljs            # regenerate index.html from the registry
../../../../node_modules/.bin/nbb verify_catalog.cljs
```

Standalone forks clone `kotoba-lang/{industry,html,css}` as siblings
(the same layout every venture quickstart uses). Regenerate whenever
the registry advances. See `docs/adr/0001-catalog-surface.md`.
