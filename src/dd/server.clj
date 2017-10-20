(ns dd.server
  (:require [compojure.core :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as response]))

(defroutes app-routes
  (GET "*" []
    (response/content-type
      (response/resource-response "index.html" {:root "public"})
      "text/html")))

(def handler
  (wrap-reload
    (wrap-gzip
      (wrap-defaults #'app-routes
                     (assoc site-defaults :static {:resources "public"})))))
