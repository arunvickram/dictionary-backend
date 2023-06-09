(ns dictionary-backend.service
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [muuntaja.interceptor :as mi]
            [reitit.dev.pretty :as pretty]
            [reitit.coercion.malli]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [muuntaja.core :as m]
            [dictionary-backend.db :as db]
            [ring.util.response :as ring-resp]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn home-page
  [req]
  {:status 200
   :body {:hello "there"}})

(defn get-word-info [req]
  (let [{:keys [word lang]} (:path-params req)]
    (if-let [result (db/get-word word lang)]
      {:status 200
       :body result}
      {:status 404})))

(defn get-word-inflection [req]
  (let [{:keys [word lang tags]} (:path-params req)
        tags (str/split tags #",")]
    (if-let [result (db/get-inflection-for-word word tags)]
      {:status 200
       :body result}
      {:status 404})))

(def routes [["/" {:get {:handler home-page}}]
             ["/:lang/:word" {:get {:handler get-word-info}}]
             ["/:lang/:word/tags/:tags" {:get {:handler get-word-inflection}}]])

(def router-options
  {:exception pretty/exception
   :data {:coercion reitit.coercion.malli/coercion
          :muuntaja m/instance
          :interceptors [(parameters/parameters-interceptor)
                         ;; content-negotiation
                         (muuntaja/format-negotiate-interceptor)
                         ;; encoding response body
                         (muuntaja/format-response-interceptor)
                         ;; exception handling
                         (exception/exception-interceptor)
                         ;; decoding request body
                         (muuntaja/format-request-interceptor)
                         ;; coercing response bodys
                         (coercion/coerce-response-interceptor)
                         ;; coercing request parameters
                         (coercion/coerce-request-interceptor)
                         ;; multipart
                         (multipart/multipart-interceptor)]}})

(def router
  (http/router routes router-options))

;; Consumed by dictionary-backend.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::server/routes []

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::server/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::server/type :jetty
              ;;::http/host "localhost"
              ::server/port 8080
              ;; Options to pass to the container (Jetty)
              ::server/container-options {:h2c? true
                                          :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                          :ssl? false
                                          ;; Alternatively, You can specify your own Jetty HTTPConfiguration
                                          ;; via the `:io.pedestal.http.jetty/http-configuration` container option.
                                        ;:io.pedestal.http.jetty/http-configuration (org.eclipse.jetty.server.HttpConfiguration.)
                                          }})
