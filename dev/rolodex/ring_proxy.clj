;; copied from tailrecursion/ring-proxy because it seems to suffer from bit-rot

(ns rolodex.ring-proxy
  (:import
    [java.net URI] )
  (:require
    [clj-http.client         :refer [request]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [clojure.string          :refer [join split]]))

(defn prepare-cookies
  "Removes the :domain and :secure keys and converts the :expires key (a Date)
  to a string in the ring response map resp. Returns resp with cookies properly
  munged."
  [resp]
  (let [prepare #(-> (update-in % [1 :expires] str)
                     (update-in [1] dissoc :domain :secure))]
    (assoc resp :cookies (into {} (map prepare (:cookies resp))))))

(defn slurp-binary
  "Reads len bytes from InputStream is and returns a byte array."
  [^java.io.InputStream is len]
  (with-open [rdr is]
    (let [buf (byte-array len)]
      (.read rdr buf)
      buf)))

(defn wrap-proxy
  "Proxies requests from proxied-path, a local URI, to the remote URI at
  remote-base-uri, also a string."
  [handler ^String proxied-path remote-base-uri & [http-opts]]
  (wrap-cookies
   (fn [req]
     (if (.startsWith ^String (:uri req) proxied-path)
       (let [rmt-full   (URI. (str remote-base-uri "/"))
             rmt-path   (URI. (.getScheme    rmt-full)
                              (.getAuthority rmt-full)
                              (.getPath      rmt-full) nil nil)
             lcl-path   (URI. (subs (:uri req) (.length proxied-path)))
             remote-uri (.resolve rmt-path lcl-path) ]
         (-> (merge {:method (:request-method req)
                     :url (str remote-uri "?" (:query-string req))
                     :headers (dissoc (:headers req) "host" "content-length")
                     :body (if-let [len (get-in req [:headers "content-length"])]
                             (when (:body req)
                               (slurp-binary (:body req) (Integer/parseInt len))))
                     :follow-redirects true
                     :throw-exceptions false
                     :as :stream} http-opts)
             request
             prepare-cookies))
       (handler req)))))
