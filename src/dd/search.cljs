(ns dd.search
  #_ (:require
    cljsjs.lunrjs))

(defprotocol ISearchable
  (reference [this])
  (fields [this])
  (add! [this args])
  (search [this query]))

(defprotocol ISearchNode
  (add-index! [this id config])
  (search-index [this index-id query]))

(defn js->json [obj]
  (js/JSON.parse (js/JSON.stringify obj)))

#_ (defn create-index [{:keys [fields documents] :as config}]
  (let [index (js/lunr (fn []
                         (this-as this
                                  (when-some [reference (name (get config :ref))]
                                    (.ref this reference))
                                  (when-not (empty? fields)
                                    (doseq [field fields]
                                      (.field this (name field))))
                                  (when-not (empty? documents)
                                    (doseq [document documents]
                                      (.add this (clj->js document)))))))]
    (reify ISearchable
                 (reference [_] reference)
                 (fields [_] fields)
                 (add! [_ args]
                   (doseq [document args]
                     (.add index (clj->js document))))
                 (search [_ query]
                   (map (fn [result]
                          (let [json (js->json result)]
                            (merge {:match-data (js->clj (.-matchData json) :keywordize-keys true)}
                                   (dissoc (js->clj json :keywordize-keys true) :matchData))))
                        (.search index query))))))

#_ (defn create-node
  ([] (create-node nil))
  ([config]
   (let [indexes (atom (reduce
                         (fn [m id]
                           (assoc m id (create-index (get config id))))
                         {}
                         (keys config)))]
     (reify ISearchNode
       (add-index! [_ index-id index-config]
         (let [index (create-index index-config)]
           (swap! indexes assoc index-id index)
           index))
       (search-index [this index-id query]
         (if-not (sequential? index-id)
           (search (get @indexes index-id) query)
           (sort-by :score > (mapcat (fn [index]
                                       (map (partial merge {:index index})
                                            (search-index this index query)))
                                     index-id))))))))
