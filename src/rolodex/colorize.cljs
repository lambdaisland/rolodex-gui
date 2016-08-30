(ns rolodex.colorize)

(declare colorized)

(defn- colorized-map [x]
  [:span
   [:span.color.curly "{"]
   (interpose ","
              (for [[k v] x]
                [:span (colorized k) " " (colorized v)]))
   [:span.color.curly "}"]])

(defn- colorized-seq [open close x]
  [:span
   [:span.color.bracket open]
   (interpose "," (map colorized x))
   [:span.color.bracket close]])

(defn colorized [x]
  (cond
    (map? x) (colorized-map x)
    (vector? x) (colorized-seq "[" "]" x)
    (seq? x) (colorized-seq "(" ")" x)
    (keyword? x) [:span.color.keyword (pr-str x)]
    (string? x) [:span.color.string (pr-str x)]
    (uuid? x) [:span.color.uuid [:span.tag "#uuid"] " \"" (str x) "\""]
    :else (pr-str x)))
