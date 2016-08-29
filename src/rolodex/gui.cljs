(ns rolodex.gui
  (:require [reagent.core :as r]
            [ajax.core :as ajax]
            [ajax.edn]
            [cljs.reader :as edn]))

(enable-console-print!)

(defonce app-state (r/atom {:fields {}
                            :contacts {}
                            :prefix "/api"
                            :last-request []
                            :last-response nil}))

(defn request [method uri opts]
  (swap! app-state assoc :last-request [method uri (:params opts)])
  (swap! app-state assoc :last-response nil)
  (ajax/easy-ajax-request uri
                          method
                          (merge
                           {:format prn-str
                            :response-format ajax.edn/edn-read}
                           (update opts :handler (fn [h]
                                                   (fn [res]
                                                     (swap! app-state assoc :last-response res)
                                                     (when h
                                                       (h res))))))))

(def GET (partial request "GET"))
(def POST (partial request "POST"))
(def PUT (partial request "PUT"))
(def DELETE (partial request "DELETE"))

(defn add-contact! [c]
  (swap! app-state update :contacts assoc (:id c) c))

(defn contacts [app]
  (vals (:contacts app)))

(defn find-contact [id]
  (get-in @app-state [:contacts id]))

(defn selected-contact [contacts]
  (first (filter :selected  contacts)))

(defn select! [contact]
  (swap! app-state assoc :fields contact))

(defn remove-contact! [id]
  (swap! app-state update :contacts dissoc id))

(defn prep-contact [contact]
  (-> contact
      (select-keys [:id :full-name :email :twitter])
      (update :id uuid)))

(defn refresh! []
  (GET "/api" {:handler (fn [res]
                          (swap! app-state assoc :contacts {})
                          (doseq [c res]
                            (add-contact! c)))}))

(defn reset-fields! []
  (swap! app-state assoc :fields {}))


(defn new-random-uuid! []
  (swap! app-state assoc-in [:fields :id] (str (random-uuid))))

(defn update-field! [key value]
  (swap! app-state assoc-in [:fields key] value))

(defn initial [contact]
  (first (:full-name contact)))

(defn contacts-by-initials [contacts]
  (reduce (fn [m [i c]]
            (update m i conj c))
          (sorted-map)
          (map (juxt initial identity) contacts)))

(defn TextField [key value]
  [:input.value {:type "text"
                 :value value
                 :on-change #(update-field! key (.. % -target -value))}])

(defn Contact [{:keys [id full-name] :as contact}]
  [:div.contact
   [:div.full-name {:on-click #(select! contact)} (if (empty? full-name) "<no name>" full-name)]])

(defn CardEntry [label contact key & more]
  [:div.card-entry
   ^{:key "label"} [:div.label label]
   ^{:key "field"} [TextField key (key contact)]
   more])

(defn Card [prefix fields]
  (let [id (str (:id fields))
        path (str prefix "/" id)
        disabled (empty? id)]
    [:div.card

     ^{:key "buttons"}
     [:div.buttonrow
      ^{:key "clear"}
      [:button {:on-click reset-fields!} "❎"]

      ^{:key "C"}
      [:button {:on-click #(POST prefix {:params (prep-contact fields)})} "POST " prefix]

      [:div.rud-butts
       ^{:key "R"}
       [:button {:disabled disabled
                 :on-click #(GET path {:handler select!})} "GET " path]

       ^{:key "U"}
       [:button {:disabled disabled
                 :on-click #(PUT path {:params (prep-contact fields)})} "PUT " path]

       ^{:key "D"}
       [:button {:disabled disabled
                 :on-click #(DELETE path)} "DELETE " path]]

                        ]
     ^{:key ":id"} [CardEntry ":id" fields :id
                    ^{:key "random-uuid"} [:button {:on-click new-random-uuid!} "random-uuid"]]
     ^{:key ":full-name"} [CardEntry ":full-name" fields :full-name]
     ^{:key ":email"} [CardEntry ":email" fields :email]
     ^{:key ":twitter"} [CardEntry ":twitter" fields :twitter]

     ]))

(defn NameList [prefix contacts]
  [:div.name-list
   [:button {:on-click refresh!} "GET " prefix]
   (for [[i cs] (contacts-by-initials contacts)]
     [:div.initial {:key i}
      [:div.letter {:key "initial"} i]
      [:div.contacts {:key "contacts"}
       (for [c cs]
         ^{:key (:id c)} [Contact c])]])])

(defn Rolodex [app-state]
  (let [app @app-state
        contacts (contacts app)
        fields (:fields app)
        prefix (:prefix app)
        [method uri req-body] (:last-request app)
        response (:last-response app)]
    [:div.app
     [:div.header
      [:div.brand "Rolodex"]
      [:input.search {:type "text" :placeholder "🔎 Search"}]

      ]
     [:div.rolodex
      [NameList prefix contacts]
      [Card prefix fields]]
     [:pre method " " uri]
     [:pre (prn-str req-body)]
     [:pre (prn-str response)]]))

(r/render [Rolodex app-state] (js/document.getElementById "gui"))
