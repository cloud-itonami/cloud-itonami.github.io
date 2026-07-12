;; In-browser filtering of the fleet catalog -- ClojureScript run by
;; scittle (no build step, no hand-written .js), same pattern as the
;; venture demo pages. Data is the JSON the generator embedded from the
;; kotoba-lang/industry registry.
(ns fleet.catalog)

(def entries
  (js->clj (js/JSON.parse (.-textContent (js/document.getElementById "catalog-data")))
           :keywordize-keys true))

(defn- esc [s]
  (-> (str s)
      (.replaceAll "&" "&amp;")
      (.replaceAll "<" "&lt;")
      (.replaceAll ">" "&gt;")))

(defn- row-html [e]
  (str "<tr><td>" (esc (:id e)) "</td>"
       "<td>" (esc (:name e)) "</td>"
       "<td>" (if (:repo e)
                (str "<a href=\"" (esc (:repo e)) "\">repo</a>") "—") "</td>"
       "<td>" (if (:demo e)
                (str "<a href=\"" (esc (:demo e)) "\">demo</a>") "—") "</td></tr>"))

(defn- matches? [e q]
  (or (= q "")
      (.includes (.toLowerCase (str (:id e) " " (:name e))) q)))

(defn- render! []
  (let [q (.toLowerCase (.-value (js/document.getElementById "q")))
        hits (filter #(matches? % q) entries)]
    (set! (.-innerHTML (js/document.getElementById "rows"))
          (apply str (map row-html hits)))
    (set! (.-hidden (js/document.getElementById "empty")) (boolean (seq hits)))))

(.addEventListener (js/document.getElementById "q") "input" render!)
(render!)
