(ns rolodex.gui
  (:require [ajax.core :as ajax]
            ajax.edn
            [reagent.core :as r]
            [rolodex.colorize :refer [colorized]]))

(enable-console-print!)

(defonce app-state (r/atom {:fields {}
                            :contacts {}
                            :prefix "/api"
                            :last-request []
                            :last-response []}))

(defn request [method uri opts]
  (swap! app-state assoc :last-request [method uri (:params opts)])
  (swap! app-state assoc :last-response nil)
  (ajax/easy-ajax-request uri
                          method
                          (merge
                           {:format prn-str
                            :response-format ajax.edn/edn-read
                            :error-handler (fn [{:keys [status status-text response] :as err}]
                                             (swap! app-state assoc :last-response [status status-text response]))}
                           (update opts :handler (fn [h]
                                                   (fn [res]
                                                     (swap! app-state assoc :last-response [200 "OK" res])
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

(defn reset-contacts! []
  (swap! app-state assoc :contacts {}))


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

     ^{:key "buttons1"}
     [:div.buttonrow
      [:div.rud-butts
       ^{:key "C"} [:button {:title "Create a new contact" :on-click #(POST prefix {:params (prep-contact fields)})} "POST"]
       [:div.path [:code prefix]]]]

     ^{:key "buttons2"}
     [:div.buttonrow

      [:div.rud-butts
       ^{:key "R"}
       [:button {:disabled disabled
                 :title "GET the contact with this id"
                 :on-click #(GET path { :handler select!})} "GET"]

       ^{:key "U"}
       [:button {:disabled disabled
                 :title "Update the contact"
                 :on-click #(PUT path {:params (prep-contact fields)})} "PUT"]

       ^{:key "D"}
       [:button {:disabled disabled
                 :title "Delete the contact"
                 :on-click #(DELETE path)} "DELETE"]

       [:div.path [:code path]]]]

     ^{:key ":id"} [CardEntry ":id" fields :id
                    ^{:key "random-uuid"} [:button {:on-click new-random-uuid!} "random-uuid"]]
     ^{:key ":full-name"} [CardEntry ":full-name" fields :full-name]
     ^{:key ":email"} [CardEntry ":email" fields :email]
     ^{:key ":twitter"} [CardEntry ":twitter" fields :twitter]

      ^{:key "clear"} [:button.clear {:title "Clear fields" :on-click reset-fields!} "❎"]

     ]))

(defn NameList [prefix contacts fields]
  [:div.name-list-wrapper
   [:div.buttons
    ^{:key "refresh"} [:button.refresh {:title "Get all the contacts" :on-click refresh!} "GET "]
    ^{:key "path"} [:div.path [:code prefix]]]

   [:div.name-list
    (for [[i cs] (contacts-by-initials contacts)]
      [:div.initial {:key i}
       [:div.letter {:key "initial"} i]
       [:div.contacts {:key "contacts"}
        (for [c cs]
          ^{:key (:id c)} [Contact c])]])]
   ^{:key "clear"} [:button.clear {:title "Clear the list of contacts" :on-click reset-contacts!} "❎"]])

(defn Rolodex [app-state]
  (let [app @app-state
        contacts (contacts app)
        fields (:fields app)
        prefix (:prefix app)
        [method uri req-body] (:last-request app)
        [status status-text response] (:last-response app)]
    [:div.app
     [:div.header
      [:div.brand "Rolodex"]
      [:input.search {:type "text" :placeholder "🔎 Search"}]

      ]
     [:div.rolodex
      [NameList prefix contacts fields]
      [Card prefix fields]]
     [:h3 "Last request"]
     [:pre.underline "→ " method " " uri]
     (if req-body
       [:pre (colorized req-body)])
     [:pre.underline "← " status " " status-text]
     (if response
       [:pre (colorized response)])]))

(r/render [Rolodex app-state] (js/document.getElementById "gui"))
