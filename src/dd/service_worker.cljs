(ns dd.service-worker)

(def *cache-name* "dd")

(def *urls-to-cache* #js
  ["/"
   "/images/maze.svg"
   "/css/style.css"
   "/js/compiled/dd.js"
   "https://unpkg.com/tachyons@4.8.1/css/tachyons.min.css"])

(defn cached-fetch! [url]
  (-> (js/fetch url)
      (.then
        (fn [res]
          (if-not (.-ok res)
            (throw (js/Error (.-statusText res)))
            (.then
              (js/caches.open *cache-name*)
              (fn [cache]
                (.put cache url (.clone res))
                res)))))
      (.catch #(.log js/console %))))

(js/self.addEventListener "install"
  (fn [e]
    (.waitUntil e
      (.then
        (js/caches.open *cache-name*)
        (fn [cache]
          (.then
            (.addAll cache *urls-to-cache*)
            #(js/self.skipWaiting)))))))

(js/self.addEventListener "activate"
  (fn [e]
    (.waitUntil e (js/self.clients.claim))))

(js/self.addEventListener "fetch"
  (fn [e]
    (.respondWith e
      (.then
        (js/caches.match (.-request e))
        (fn [res]
          (or res (cached-fetch! (.-request e))))))))
