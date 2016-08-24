(ns rolodex.gui
  (:require [reagent.core :as r]
            [ajax.core :as ajax]
            [ajax.edn]
            [cljs.reader :as edn]))

(enable-console-print!)


(defonce app-state (r/atom {:contacts {}
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
                                                     (h res)))))))

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

(defn select! [contact-id]
  (swap! app-state (fn [app]
                     (let [c (selected-contact (contacts app))]
                       (cond-> app
                         c (update-in [:contacts (:id c)] dissoc :selected)
                         contact-id (assoc-in [:contacts contact-id :selected] true))))))

(defn toggle-edit! []
  (swap! app-state update :editing not))

(defn start-editing! []
  (swap! app-state assoc :editing true))

(defn stop-editing! []
  (swap! app-state assoc :editing false))

(defn remove-contact! [id]
  (swap! app-state update :contacts dissoc id))

(defn submit-contact! [id]
  (let [contact (find-contact id)
        filtered (select-keys contact [:id :full-name :email :twitter])
        args {:format prn-str :params filtered}]
    (if (:unsaved contact)
      (do
        (POST "/api" (assoc args :handler (fn [saved]
                                            (remove-contact! id)
                                            (add-contact! saved)))))
      (PUT (str "/api/" id) args))))


(defn refresh! []
  (GET "/api" {:handler (fn [res]
                          (swap! app-state assoc :contacts {})
                          (doseq [c res]
                            (add-contact! c)))})
  (stop-editing!))

(defn update-card! [id key value]
  (swap! app-state assoc-in [:contacts id key] value))

(defn initial [contact]
  (first (:full-name contact)))

(defn contacts-by-initials [contacts]
  (reduce (fn [m [i c]]
            (update m i conj c))
          (sorted-map)
          (map (juxt initial identity) contacts)))

(defn TextField [id key value]
  [:input.value {:type "text"
                 :value value
                 :on-change #(update-card! id key (.. % -target -value))}])

(defn Contact [{:keys [id full-name selected] :as contact}]
  [:div.contact {:class (if selected "selected")}
   [:div.full-name {:on-click #(if selected (select! nil) (select! id))} (if (empty? full-name) "<no name>" full-name)]])

(defn CardEntry [label contact key editing]
  [:div.card-entry
   [:div.label label]
   (if editing
     [TextField (:id contact) key (key contact)]
     [:div.value (key contact)])])

(defn Card [contact editing]
  [:div.card

   ^{:key "Name"} [CardEntry "Name" contact :full-name editing]
   ^{:key "Email"} [CardEntry "Email" contact :email editing]
   ^{:key "Twitter"} [CardEntry "Twitter" contact :twitter editing]
   ^{:key "Skills"} [CardEntry "Skills" contact :skills editing]
   [:div.buttonrow
    [:button {:disabled (not contact)
              :on-click (fn []
                          (DELETE (str "/api/" (:id contact)))
                          (refresh!))} "❎"]
    [:button {:disabled (not contact)
                          :on-click (fn []
                                      (when editing (submit-contact! (:id contact)))
                                      (toggle-edit!))} (if editing "✔️" "✏️")]

    [:span.uuid (str (:id contact))]]

   [:pre (prn-str contact)]])

(defn NameList [contacts]
  [:div.name-list
   (for [[i cs] (contacts-by-initials contacts)]
     [:div.initial {:key i}
      [:div.letter {:key "initial"} i]
      [:div.contacts {:key "contacts"}
       (for [c cs]
         ^{:key (:id c)} [Contact c])]])])

(defn Rolodex [app-state]
  (let [app @app-state
        contacts (contacts app)
        selected (selected-contact contacts)
        [method uri req-body] (:last-request app)
        response (:last-response app)]
    [:div.app
     [:div.header
      [:div.brand "Reagodex"]
      [:input.search {:type "text" :placeholder "🔎 Search"}]
      [:button {:on-click #(let [id "new contact"]
                             (add-contact! {:id id :unsaved true})
                             (select! id)
                             (start-editing!))  } "🚹"]
      [:button {:on-click refresh!} "🔄"]]
     [:div.rolodex
      [NameList contacts]
      [Card selected (:editing app)]]
     [:pre method " " uri]
     [:pre (prn-str req-body)]
     [:pre (prn-str response)]]))

(r/render [Rolodex app-state] (js/document.getElementById "gui"))
