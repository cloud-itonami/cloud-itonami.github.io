;; Generates ../index.html -- the cloud-itonami fleet catalog served at
;; https://cloud-itonami.github.io/ (the org's root Pages site, the same
;; host that already serves the per-venture demos under
;; /cloud-itonami-isic-*/). Markup/styling as data via kotoba-lang/html
;; + kotoba-lang/css (nbb authoring, zero build step for visitors;
;; in-browser filtering is `catalog.cljs` run by scittle) -- the same
;; pattern as the venture demo pages.
;;
;; The catalog is NOT hand-typed: it is generated from
;; kotoba-lang/industry's registry.edn -- the fleet's machine-readable
;; source of truth for maturity, repos and demo links -- so this page
;; can never drift from the registry (see docs/adr/0001).
;;
;; Run (from this web/ directory, inside the monorepo checkout;
;; standalone forks clone kotoba-lang/{industry,html,css} as siblings,
;; same layout as the venture quickstarts):
;;   ../../../../node_modules/.bin/nbb \
;;     --classpath "../../../kotoba-lang/html/src:../../../kotoba-lang/css/src" \
;;     generate.cljs [path-to-registry.edn]
(require '[clojure.edn :as edn]
         '[html.core :as html]
         '[css.core :as css]
         '["fs" :as fs])

(def registry-path
  (or (first *command-line-args*)
      "../../../kotoba-lang/industry/resources/kotoba/industry/registry.edn"))

(def registry (edn/read-string (fs/readFileSync registry-path "utf8")))
(def industries (:industries registry))
(def implemented (vec (filter #(= :implemented (:maturity %)) industries)))
(def n-implemented (count implemented))
(def n-blueprint (count (filter #(= :blueprint (:maturity %)) industries)))
(def n-total (count industries))
;; :spec = everything not yet at blueprint/implemented tier (matches
;; kotoba.industry/maturity-of's default-to-:spec semantics closely
;; enough for an honest headline count).
(def n-spec (- n-total n-implemented n-blueprint))

(defn entry->json [e]
  {:id (:id e) :name (:name e)
   :repo (:repo e)
   :demo (:demo e)})

(def demo-urls (vec (keep :demo industries)))

(def stylesheet
  (css/style-node
   {:rules
    {":root" {:--fg "#1b1f24" :--bg "#ffffff" :--muted "#57606a"
              :--card "#f6f8fa" :--line "#d0d7de" :--accent "#0b5cad"
              :--ok-bg "#dafbe1" :--ok-fg "#116329"}
     "body" {:font-family "system-ui,-apple-system,'Hiragino Sans','Noto Sans JP',sans-serif"
             :margin "0 auto" :max-width 880 :padding "28px 20px 48px"
             :color "var(--fg)" :background "var(--bg)" :line-height 1.55}
     "header p.sub" {:color "var(--muted)" :margin-top 4}
     "h1"   {:font-size 24 :margin "0"}
     "h2"   {:font-size 17 :margin-top 40 :border-top "1px solid var(--line)"
             :padding-top 24}
     ".badge" {:display :inline-block :font-size 12 :font-weight 600
               :border-radius 20 :padding "2px 10px" :margin-left 8
               :vertical-align "1px" :background "var(--ok-bg)" :color "var(--ok-fg)"}
     ".card" {:background "var(--card)" :border "1px solid var(--line)"
              :border-radius 10 :padding "14px 16px" :margin-top 12}
     ".card h3" {:margin "0 0 2px" :font-size 16}
     ".card .meta" {:color "var(--muted)" :font-size 13.5}
     ".search" {:display :flex :gap 8 :margin-top 20}
     "input#q" {:flex 1 :font-size 16 :padding "10px 14px"
                :border "1.5px solid var(--line)" :border-radius 8
                :background "var(--bg)" :color "var(--fg)"}
     "table" {:border-collapse :collapse :width "100%" :margin-top 12
              :font-size 13.5}
     "th" {:text-align :left :color "var(--muted)" :font-weight 600
           :border-bottom "1.5px solid var(--line)" :padding "6px 8px"}
     "td" {:border-bottom "1px solid var(--line)" :padding "7px 8px"
           :vertical-align :top}
     "#empty" {:color "var(--muted)" :margin-top 16}
     "footer" {:margin-top 48 :padding-top 16 :border-top "1px solid var(--line)"
               :color "var(--muted)" :font-size 13.5}
     "a" {:color "var(--accent)"}
     "code" {:background "var(--card)" :padding "1px 5px" :border-radius 4
             :font-size "0.9em"}}
    :media
    {"(prefers-color-scheme: dark)"
     {":root" {:--fg "#e6edf3" :--bg "#0d1117" :--muted "#8d96a0"
               :--card "#161b22" :--line "#30363d" :--accent "#58a6ff"
               :--ok-bg "#12261e" :--ok-fg "#3fb950"}}}}))

(def page
  [:html {:lang "ja"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:title "cloud-itonami — governed OSS business fleet catalog"]
    [:meta {:name "description"
            :content "業種ごとの governed OSS ビジネス実装カタログ。LLM advisor は提案のみ、独立ガバナーが検閲、全操作が監査台帳に残る。AGPL で fork 可。"}]
    stylesheet]
   [:body
    [:header
     [:h1 "cloud-itonami fleet " [:span.badge (str n-implemented " implemented")]]
     [:p.sub "業種(ISIC)ごとの governed OSS ビジネス実装カタログ。この一覧は "
      [:a {:href "https://github.com/kotoba-lang/industry"} "kotoba-lang/industry"]
      " のレジストリ(機械可読の SSoT、全 " n-total " 業種)から生成されています — 手書きではありません。"
      "各実装は同一アーキテクチャ: LLM advisor は提案のみ・独立ガバナー(HARD check は人間でも覆せない)・"
      "実世界の行為は常時人間承認・追記専用監査台帳。AGPL-3.0 で fork して自分の事業として運営できます。"]
     [:p.sub "成熟度の内訳(正直な現在地): implemented " n-implemented
      " · blueprint " n-blueprint " · spec " n-spec " / 全 " n-total " 業種。"]]

    [:h2 "Sales wedge — 購入・運用支援する旗艦業種"]
    [:p.sub "cloud-itonami が直接販売・運用支援するのは下記の旗艦ペア（Indeed 型 / kaonavi 型）です。"
      "狭い楔(wedge)としてここに集中し、残りの実装は下記 "
      [:strong "Library"] "（fork 自由・直接販売対象外）として公開します。"]
    [:div.card
     [:h3 [:a {:href "/cloud-itonami-isic-6399/"} "Meta Job Search"] " — Indeed 型求人アグリゲーターの置き換え"]
     [:p.meta "掲載拒否理由(募集終了・賃金不一致・転載許諾・差別広告)を実判定つきで公開。"
      "訂正・取下げは番号つき記録。職業安定法5条の4 をソフトウェアとして実装。6 法域対応。"]]
    [:div.card
     [:h3 [:a {:href "/cloud-itonami-isic-6310/"} "Talent Board"] " — kaonavi 型 HR SaaS の置き換え"]
     [:p.meta "保護属性は評価・配置転換の根拠にできず、帳票は目的に許された列のみ。"
      "人材データは自分の Store に残る。配置転換は常時人間承認 + from/to/承認者の台帳化。"]]

    [:h2 (str "Library — 全 actor 実装（fork 自由・直接販売対象外）· " n-implemented " implemented")]
    [:div.search
     [:input {:id "q" :type "search" :placeholder "ISIC コード・名前で絞り込み…" :autocomplete "off"}]]
    [:table
     [:thead [:tr [:th "ISIC"] [:th "name"] [:th "repo"] [:th "demo"]]]
     [:tbody {:id "rows"}]]
    [:p {:id "empty" :hidden true} "該当する actor はありません。"]

    [:h2 "operator になるには"]
    [:ol
     [:li "vertical を選び、デモとテストスイートを動かす"]
     [:li "その repo の " [:code "docs/operator-quickstart.md"] " に従って fork → 自分のデータ → 自分のデプロイ"]
     [:li "各 repo の " [:code "docs/business-model.md"] " に価格の形・ユニットエコノミクス・認証ラダー(itonami.cloud)"]
     [:li "認証・managed 提供・導入支援に関心があれば、各 repo の "
      [:strong "operator-interest issue"] " から連絡(例: "
      [:a {:href "https://github.com/cloud-itonami/cloud-itonami-isic-6399/issues/new?template=operator-interest.yml"} "Meta Job Search"]
      " / "
      [:a {:href "https://github.com/cloud-itonami/cloud-itonami-isic-6310/issues/new?template=operator-interest.yml"} "Talent Board"]
      " / "
      [:a {:href "https://github.com/cloud-itonami/cloud-itonami-isic-7810/issues/new?template=operator-interest.yml"} "Placement Desk"]
      ")"]]

    [:footer
     [:p "このページは " [:code "web/generate.cljs"] " (nbb) が registry から生成し、絞り込みは "
      [:code "catalog.cljs"] " (scittle = ブラウザ内 ClojureScript) が実行しています。 "
      [:a {:href "https://github.com/cloud-itonami/cloud-itonami.github.io"} "source"]
      " · "
      [:a {:href "https://github.com/cloud-itonami"} "GitHub org"]]]

    [:script {:type "application/json" :id "catalog-data"}
     [:hiccup/raw (js/JSON.stringify (clj->js (mapv entry->json implemented)))]]
    [:script {:src "https://cdn.jsdelivr.net/npm/scittle@0.6.22/dist/scittle.js"}]
    [:script {:type "application/x-scittle" :src "catalog.cljs"}]]])

(fs/writeFileSync "../index.html" (str "<!doctype html>\n" (html/render page) "\n"))
(fs/copyFileSync "catalog.cljs" "../catalog.cljs")

;; sitemap.xml / robots.txt -- generated from the same registry.edn :demo links
;; the catalog table renders (never hand-typed, can't drift from the registry).
;; Only the 3 flagships carry a :demo entry today; as more verticals earn one
;; (product-score climbs, ADR-2607121700-style), they appear here automatically.
(def sitemap-urls (into ["https://cloud-itonami.github.io/"] demo-urls))
(defn url-entry [u] (str "  <url><loc>" u "</loc></url>"))
(fs/writeFileSync "../sitemap.xml"
                   (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
                        (apply str (map #(str (url-entry %) "\n") sitemap-urls))
                        "</urlset>\n"))
(fs/writeFileSync "../robots.txt"
                   (str "User-agent: *\nAllow: /\nSitemap: https://cloud-itonami.github.io/sitemap.xml\n"))

(println (str "wrote index.html (" n-implemented " implemented / " n-total " industries), "
              "sitemap.xml (" (count sitemap-urls) " urls), robots.txt"))
