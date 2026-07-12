;; Headless verification for the fleet catalog -- stubs the DOM surface
;; catalog.cljs touches, feeds it the REAL JSON block from the generated
;; index.html, loads catalog.cljs, and asserts rendering + filtering.
;; nbb script (Node-harnesses-in-nbb rule), same pattern as the venture
;; demo harnesses.
;;
;; Run (from this web/ directory):
;;   ../../../../node_modules/.bin/nbb verify_catalog.cljs
(require '["fs" :as fs])

(def html (fs/readFileSync "../index.html" "utf8"))

(def json-block
  (let [m (re-find #"<script type=\"application/json\" id=\"catalog-data\">(\[.*?\])</script>" html)]
    (or (second m) (throw (js/Error. "catalog-data JSON block not found")))))

(def listeners (atom {}))
(defn- el [id init]
  (let [o (js-obj)]
    (doseq [[k v] init] (aset o k v))
    (aset o "addEventListener" (fn [ev f] (swap! listeners assoc [id ev] f)))
    o))

(def elements
  {"catalog-data" (el "catalog-data" {"textContent" json-block})
   "q"            (el "q" {"value" ""})
   "rows"         (el "rows" {"innerHTML" ""})
   "empty"        (el "empty" {"hidden" true})})

(aset js/globalThis "document" (js-obj "getElementById" (fn [id] (get elements id))))
(load-string (fs/readFileSync "../catalog.cljs" "utf8"))

(defn- rows-html [] (aget (get elements "rows") "innerHTML"))
(defn- assert! [ok? msg]
  (if ok? (println "ok  " msg) (do (println "FAIL" msg) (js/process.exit 1))))

(assert! (.includes (rows-html) "6399") "catalog renders isic-6399")
(assert! (.includes (rows-html) "6310") "catalog renders isic-6310")
(assert! (.includes (rows-html) "cloud-itonami.github.io/cloud-itonami-isic-6399") "demo link present for 6399")
(assert! (> (.-length (.split (rows-html) "<tr>")) 100) "catalog has 100+ rows")

(aset (get elements "q") "value" "job search")
((get @listeners ["q" "input"]))
(assert! (.includes (rows-html) "Meta Job Search") "filter finds Meta Job Search")
(assert! (not (.includes (rows-html) "Talent Actor")) "filter excludes non-matches")

(aset (get elements "q") "value" "zzz-nothing")
((get @listeners ["q" "input"]))
(assert! (= "" (rows-html)) "no-hit filter renders no rows")
(assert! (false? (boolean (aget (get elements "empty") "hidden"))) "no-hit reveals empty notice")

;; the header counts are registry-derived, not hand-typed
(assert! (.includes html "implemented") "implemented count badge present")

(println "verify_catalog: all assertions passed")
